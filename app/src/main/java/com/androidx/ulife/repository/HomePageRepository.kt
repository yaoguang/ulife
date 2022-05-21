package com.androidx.ulife.repository

import android.util.SparseArray
import com.androidx.ulife.dao.*
import com.androidx.ulife.model.*
import com.androidx.ulife.net.GrpcApi
import com.androidx.ulife.net.RetrofitApi
import com.androidx.ulife.net.SuspendCallBack
import com.androidx.ulife.simcard.SimCardManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

object HomePageRepository {
    private val homePageDao by lazy { AppDatabase.appDb.homePageDao() }
    private val homeUssdDao by lazy { AppDatabase.appDb.homeUssdDao() }

    private val partCache = SparseArray<HomePagePart>()
    private val ussdPartCache = HashMap<String, HomeUssdPart>()

    fun homePageInfo(): Flow<HomePagePart> {
        val partRequest = homePageDao.queryPartReqList()
        updateUssdRequest(partRequest)
        return GrpcApi.homePageInfo(partRequest)
            .mapNotNull {
                val request = partRequest.firstOrNull { req -> it.partType == req.partType } ?: return@mapNotNull null
                convertResponseData(request, it)
            }
    }

    fun homePageInfo(reqParts: ArrayList<Int>): Flow<HomePagePart> {
        val requests = homePageDao.queryPartReqList(reqParts)
        val partRequest = reqParts.map {
            requests.firstOrNull { req -> req.partType == it } ?: HomePagePartRequest(null, it, 0, 0L)
        }
        updateUssdRequest(partRequest)
        return GrpcApi.homePageInfo(partRequest)
            .mapNotNull {
                val request = partRequest.firstOrNull { req -> it.partType == req.partType } ?: return@mapNotNull null
                convertResponseData(request, it)
            }
    }

    private fun convertResponseData(request: HomePagePartRequest, response: UlifeResp.QueryResponse): HomePagePart? {
        val it: HomePagePart = convertNormalResponseData(request, response)
        when (request.partType) {
            PART_TYPE_USSD -> convertUssdResponseData(request, it, response)
        }
        return it
    }

    private fun convertNormalResponseData(request: HomePagePartRequest, it: UlifeResp.QueryResponse): HomePagePart {
        val part: HomePagePart
        // 未返回有效信息，仅更新数据库请求参数
        if (it.dataPartCase == UlifeResp.QueryResponse.DataPartCase.DATAPART_NOT_SET) {
            // req 是数据库提供的请求参数时，更新数据库；是自己构建的请求参数时，无数据不做处理，后续请求继续自己构建
            request.updateTime = it.updateTime
            homePageDao.updateReqPart(request)

            // 读取内存缓存
            var cachePart = partCache[request.partType]
            // 更新内存缓存信息
            if (cachePart != null)
                cachePart.updateTime = it.updateTime

            // 判断缓存可用，不可用的话，读取数据库缓存；因为已更新了updateTime，所以不需要再次更新
            if (cachePart == null) {
                cachePart = homePageDao.queryPart(request.partType)
            }

            // 缓存不存在，使用简单返回值
            if (cachePart == null) {
                cachePart = HomePagePart(request.id, request.partType, request.version, request.updateTime, it.code, HomePagePartForm.NET.ordinal, it.toByteArray()).apply { dataProto = it }
            }
            part = cachePart
        } else {
            // 返回有效信息，需要更新cache缓存，更新数据库参数
            part = HomePagePart(
                null,
                it.partType,
                it.version,
                it.updateTime,
                RefreshMode.ON_RESUME.ordinal,
                HomePagePartForm.NET.ordinal,
                if (it.partType == PART_TYPE_USSD) null else it.toByteArray()
            )
            part.dataProto = it
            // 更新数据库
            homePageDao.insert(part)
        }
        partCache[it.partType] = part
        return part
    }

    private fun convertUssdResponseData(request: HomePagePartRequest, part: HomePagePart, response: UlifeResp.QueryResponse) {
        val imsiListPart = HomeImsiListPart()
        val ussdPart = if (response.hasUssdPart()) response.ussdPart else null
        if (request.imsi1 != null)
            imsiListPart.imsi1 = convertUssdImsiResponseData(request.imsi1!!, response, ussdPart?.imsi1, 1)
        if (request.imsi2 != null)
            imsiListPart.imsi2 = convertUssdImsiResponseData(request.imsi2!!, response, ussdPart?.imsi2, 2)
        part.dataPart = imsiListPart
        part.dataProto = null
        part.dataArray = null
    }

    private fun convertUssdImsiResponseData(request: HomeUssdPartRequest, response: UlifeResp.QueryResponse, imsi: UlifeResp.ImsiPart?, simIndex: Int): HomeUssdPart {
        val part: HomeUssdPart
        // 当前卡号未返回有效数据,需要读取缓存数据，返回接口使用，更新数据库请求时间
        if (imsi == null || imsi.dataSetList.isNullOrEmpty()) {
            // 更新数据库请求时间
            request.updateTime = response.updateTime
            homeUssdDao.updateReqPart(request)

            // 读取内存缓存
            var cachePart = ussdPartCache[request.mccMnc]
            // 更新内存缓存信息
            if (cachePart != null)
                cachePart.updateTime = request.updateTime

            // 判断缓存可用，不可用的话，读取数据库缓存；因为已更新了updateTime，所以不需要再次更新
            if (cachePart == null) {
                cachePart = homeUssdDao.queryPart(request.mcc, request.mnc)
            }

            // 缓存不存在，使用简单返回值
            if (cachePart == null) {
                cachePart = HomeUssdPart(request.id, request.mcc, request.mnc, request.version, request.updateTime, HomePagePartForm.NET.ordinal, imsi?.toByteArray()).apply { dataProto = imsi }
            }
            part = cachePart
        } else {
            // 当前卡号返回了有效数据,需要写入缓存数据，返回接口使用，更新数据库imsi数据内容
            // 写入缓存后续统一处理
            // 更新数据库imsi数据内容
            part = HomeUssdPart(
                null,
                request.mcc, request.mnc,
                imsi.version, imsi.updateTime,
                HomePagePartForm.NET.ordinal,
                imsi.toByteArray()
            )
            part.dataProto = imsi
            homeUssdDao.insert(part)
        }
        ussdPartCache[request.mccMnc] = part
        return part
    }

    private fun updateUssdRequest(partRequest: List<HomePagePartRequest>) {
        partRequest.firstOrNull { it.partType == PART_TYPE_USSD }?.apply {
            imsi1 = createUssdRequestBySim(SimCardManager.sim1)
            imsi2 = createUssdRequestBySim(SimCardManager.sim2)
        }
    }

    private fun createUssdHomePartRequest(): HomePagePartRequest {
        return HomePagePartRequest(null, PART_TYPE_USSD, 0, 0).apply {
            imsi1 = createUssdRequestBySim(SimCardManager.sim1)
            imsi2 = createUssdRequestBySim(SimCardManager.sim2)
        }
    }

    private fun createUssdRequestBySim(sim: SimCardInfo): HomeUssdPartRequest? {
        return if (sim.isValued())
            homeUssdDao.queryPartReq(sim.mcc, sim.mnc) ?: HomeUssdPartRequest(null, sim.mcc, sim.mnc, 0, 0L)
        else null
    }

    /**
     * 协程中使用
     */
    @Throws(Exception::class)
    suspend fun test(): Int {
        return RetrofitApi.apiService.test()
    }

    /**
     * java使用
     */
    fun testJava(callback: SuspendCallBack<Int>) {
        RetrofitApi.suspendCall({ it.apiService.test() }, callback)
    }
}