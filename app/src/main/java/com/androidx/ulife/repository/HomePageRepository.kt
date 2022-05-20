package com.androidx.ulife.repository

import android.util.SparseArray
import com.androidx.ulife.dao.*
import com.androidx.ulife.model.*
import com.androidx.ulife.net.GrpcApi
import com.androidx.ulife.net.RetrofitApi
import com.androidx.ulife.net.SuspendCallBack
import com.androidx.ulife.simcard.SimCardManager
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object HomePageRepository {
    private val homePageDao by lazy { AppDatabase.appDb.homePageDao() }
    private val homeUssdDao by lazy { AppDatabase.appDb.homeUssdDao() }

    private val responseCache = SparseArray<UlifeResp.QueryResponse>()
    private var ussdResponseCache: UlifeResp.QueryResponse? = null

    fun homePageInfo(): Flow<UlifeResp.QueryResponse?> {
        val partRequest = homePageDao.queryPartReqList()
        updateUssdRequest(partRequest)
        return GrpcApi.homePageInfo(partRequest)
            .map {
                LogUtils.d("Response", it)
                val request = partRequest.firstOrNull { req -> it.partType == req.partType } ?: return@map it
                convertResponseData(request, it) ?: it
            }
    }

    fun homePageInfo(reqParts: ArrayList<Int>): Flow<UlifeResp.QueryResponse?> {
        val requests = homePageDao.queryPartReqList(reqParts)
        val partRequest = reqParts.map {
            requests.firstOrNull { req -> req.partType == it } ?: HomePagePartRequest(null, it, 0, 0L)
        }
        updateUssdRequest(partRequest)
        return GrpcApi.homePageInfo(partRequest)
            .map {
                val request = partRequest.firstOrNull { req -> it.partType == req.partType } ?: return@map it
                convertResponseData(request, it) ?: it
            }
    }

    private fun convertResponseData(request: HomePagePartRequest?, it: UlifeResp.QueryResponse): UlifeResp.QueryResponse? {
        if (request == null)
            return null
        return if (request.partType == PART_TYPE_USSD) {
            convertUssdResponseData(request, convertNormalResponseData(request, it))
        } else {
            convertNormalResponseData(request, it)
        }
    }

    private fun convertNormalResponseData(request: HomePagePartRequest, response: UlifeResp.QueryResponse): UlifeResp.QueryResponse {
        var it = response
        // 未返回有效信息，仅更新数据库请求参数
        if (it.dataPartCase == UlifeResp.QueryResponse.DataPartCase.DATAPART_NOT_SET) {
            // req 是数据库提供的请求参数时，更新数据库；是自己构建的请求参数时，无数据不做处理，后续请求继续自己构建
            request.updateTime = it.updateTime
            homePageDao.updateReqPart(request)

            // 读取内存缓存
            var cacheResp = responseCache[request.partType]
            // 判断缓存可用，不可用的话，读取数据库缓存
            if (cacheResp == null || cacheResp.dataPartCase == UlifeResp.QueryResponse.DataPartCase.DATAPART_NOT_SET) {
                cacheResp = homePageDao.queryPart(request.partType)?.toResponse()
            }
            if (cacheResp != null)
                it = cacheResp
        } else {
            // 返回有效信息，需要更新cache缓存，更新数据库参数
            val dataArray: ByteArray? = when {
                it.hasAdPicPart() -> it.adPicPart.toByteArray()
                it.hasPinnedPart() -> it.pinnedPart.toByteArray()
                it.hasUssdPart() -> it.ussdPart.toByteArray()
                it.hasTopupPart() -> it.topupPart.toByteArray()
                it.hasAdTxtPart() -> it.adTxtPart.toByteArray()
                else -> null
            }
            val part = HomePagePart(
                null,
                it.partType,
                it.version,
                it.updateTime,
                RefreshMode.ON_RESUME.ordinal,
                HomePagePartForm.NET.ordinal,
                dataArray
            )
            // 更新数据库
            homePageDao.insert(part)
        }
        responseCache[it.partType] = it
        return it
    }

    private fun convertUssdResponseData(request: HomePagePartRequest, response: UlifeResp.QueryResponse): UlifeResp.QueryResponse {
        var it = response
        val ussdPart = if (it.hasUssdPart()) it.ussdPart else null
        if (request.imsi1 != null)
            it = convertUssdImsiResponseData(request.imsi1!!, it, ussdPart?.imsi1, 1)
        if (request.imsi2 != null)
            it = convertUssdImsiResponseData(request.imsi2!!, it, ussdPart?.imsi2, 2)

        ussdResponseCache = it
        return it
    }

    private fun convertUssdImsiResponseData(request: HomeUssdPartRequest, response: UlifeResp.QueryResponse, imsi: UlifeResp.ImsiPart?, simIndex: Int): UlifeResp.QueryResponse {
        var it = response
        // 当前卡号未返回有效数据,需要读取缓存数据，返回接口使用，更新数据库请求时间
        if (imsi == null || imsi.dataSetList.isNullOrEmpty()) {
            // 更新数据库请求时间
            request.updateTime = it.updateTime
            homeUssdDao.updateReqPart(request)

            // 读取内存缓存
            val isCached = (if (simIndex == 1) ussdResponseCache?.ussdPart?.hasImsi1() else ussdResponseCache?.ussdPart?.hasImsi2())
            var cacheImsi = if (isCached == true)
                (if (simIndex == 1) ussdResponseCache?.ussdPart?.imsi1 else ussdResponseCache?.ussdPart?.imsi2)
            else null
            // 内存缓存不存在，读取数据库缓存
            if (cacheImsi == null)
                cacheImsi = homeUssdDao.queryPart(request.mcc, request.mnc)?.toUssdImsiPart()

            // 读取到可用缓存，更新返回值
            if (cacheImsi != null) {
                it = it.copy {
                    ussdPart = it.ussdPart.copy {
                        if (simIndex == 1) imsi1 = cacheImsi else imsi2 = cacheImsi
                    }
                }
            }
        } else {
            // 当前卡号返回了有效数据,需要写入缓存数据，返回接口使用，更新数据库imsi数据内容
            // 写入缓存后续统一处理
            // 更新数据库imsi数据内容
            val homeUssdPart = HomeUssdPart(
                null,
                request.mcc, request.mnc,
                imsi.version, imsi.updateTime,
                HomePagePartForm.NET.ordinal,
                imsi.toByteArray()
            )
            homeUssdDao.insert(homeUssdPart)
        }
        return it
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