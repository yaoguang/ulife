package com.androidx.ulife.repository

import android.util.SparseArray
import com.androidx.ulife.dao.*
import com.androidx.ulife.model.*
import com.androidx.ulife.net.GrpcApi
import com.androidx.ulife.net.RetrofitApi
import com.androidx.ulife.net.SuspendCallBack
import com.androidx.ulife.simcard.SimCardManager
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object HomePageRepository {
    private val homePageDao by lazy { AppDatabase.appDb.homePageDao() }
    private val homeUssdDao by lazy { AppDatabase.appDb.homeCarrierDao() }

    private val partCache = SparseArray<HomePagePart>()
    private val carrierCache = HashMap<String, HomeCarrierPart>()

    val partListState = MutableStateFlow(SparseArray<HomePagePart>())

    val partRefreshShare = MutableSharedFlow<HomePagePart>()

    fun initFlowInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            LogUtils.i("init_info", "localInfo")
            val localParts = localInfo()
            localParts.forEach {
                partCache[it.partType] = it
                updatePartInfoCache(it)
            }
            LogUtils.i("init_info", "homePageInfo")
            homePageInfo()
                .catch {
                    LogUtils.e("init_info", "init request error ", it.message)
                }
                .onCompletion {
                    LogUtils.i("init_info", "request over")
                }
                .collect()
        }
    }

    private fun updatePartInfoCache(part: HomePagePart) {
        when (part.partType) {
            PART_TYPE_USSD -> {
                val imsiListPart = part.dataPart as? HomeUssdPart ?: return
                imsiListPart.imsi1?.let { carrierCache[it.mccMnc] = it }
                imsiListPart.imsi2?.let { carrierCache[it.mccMnc] = it }
            }
        }
    }

    suspend fun localInfo(): List<HomePagePart> {
        val parts = homePageDao.queryParts()
        parts.forEach {
            when (it.partType) {
                PART_TYPE_USSD -> convertUssdLocalData(it)
            }
        }
        notifyListState(parts as ArrayList<HomePagePart>)
        return parts
    }

    private fun convertUssdLocalData(part: HomePagePart) {
        val imsiListPart = HomeUssdPart()
        if (SimCardManager.sim1.isValued())
            imsiListPart.imsi1 = convertUssdImsiLocalData(part, SimCardManager.sim1, 1)
        if (SimCardManager.sim2.isValued())
            imsiListPart.imsi2 = convertUssdImsiLocalData(part, SimCardManager.sim2, 2)
        part.dataPart = imsiListPart
        part.dataProto = null
        part.dataArray = null
    }

    private fun convertUssdImsiLocalData(part: HomePagePart, cardInfo: SimCardInfo, simIndex: Int): HomeCarrierPart {
        var cachePart = homeUssdDao.queryPart(cardInfo.mcc, cardInfo.mnc)
        // 缓存不存在，使用简单返回值
        if (cachePart == null) {
            cachePart = HomeCarrierPart(null, cardInfo.mcc, cardInfo.mnc, 0, part.updateTime, HomePagePartForm.NET.ordinal, null)
        }
        return cachePart
    }

    private suspend fun notifyListState(part: ArrayList<HomePagePart>) {
        val stateValue = partListState.value
        var notify = false
        part.filter {
            it != stateValue[it.partType]
        }.forEach {
            stateValue[it.partType] = it
            notify = true
            partRefreshShare.emit(it)
        }
        if (notify) {
            partListState.emit(stateValue.clone())
        }
    }

    private suspend fun notifyListState(part: HomePagePart) {
        val stateValue = partListState.value
        if (part != stateValue[part.partType]) {
            stateValue[part.partType] = part
            partRefreshShare.emit(part)
            partListState.emit(stateValue.clone())
        }
    }

    fun homePageInfo(): Flow<HomePagePart> {
        val partRequest = homePageDao.queryPartReqList()
        updateUssdRequest(partRequest)
        return GrpcApi.homePageInfo(partRequest)
            .mapNotNull {
                val request = partRequest.firstOrNull { req -> it.partType == req.partType } ?: return@mapNotNull null
                convertResponseData(request, it)
            }
            .onEach { notifyListState(it) }
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
            .onEach { notifyListState(it) }
    }

    private fun convertResponseData(request: HomePagePartRequest, response: UlifeResp.QueryResponse): HomePagePart {
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
        val imsiListPart = HomeUssdPart()
        val ussdPart = if (response.hasUssdPart()) response.ussdPart else null
        if (request.imsi1 != null)
            imsiListPart.imsi1 = convertUssdImsiResponseData(request.imsi1!!, response, ussdPart?.imsi1, 1)
        if (request.imsi2 != null)
            imsiListPart.imsi2 = convertUssdImsiResponseData(request.imsi2!!, response, ussdPart?.imsi2, 2)
        part.dataPart = imsiListPart
        part.dataProto = null
        part.dataArray = null
    }

    private fun convertUssdImsiResponseData(request: HomeCarrierPartRequest, response: UlifeResp.QueryResponse, imsi: UlifeResp.ImsiPart?, simIndex: Int): HomeCarrierPart {
        val part: HomeCarrierPart
        // 当前卡号未返回有效数据,需要读取缓存数据，返回接口使用，更新数据库请求时间
        if (imsi == null || imsi.dataSetList.isNullOrEmpty()) {
            // 更新数据库请求时间
            request.updateTime = response.updateTime
            homeUssdDao.updateReqPart(request)

            // 读取内存缓存
            var cachePart = carrierCache[request.mccMnc]
            // 更新内存缓存信息
            if (cachePart != null)
                cachePart.updateTime = request.updateTime

            // 判断缓存可用，不可用的话，读取数据库缓存；因为已更新了updateTime，所以不需要再次更新
            if (cachePart == null) {
                cachePart = homeUssdDao.queryPart(request.mcc, request.mnc)
            }

            // 缓存不存在，使用简单返回值
            if (cachePart == null) {
                cachePart = HomeCarrierPart(request.id, request.mcc, request.mnc, request.version, request.updateTime, HomePagePartForm.NET.ordinal, imsi?.toByteArray()).apply { dataProto = imsi }
            }
            part = cachePart
        } else {
            // 当前卡号返回了有效数据,需要写入缓存数据，返回接口使用，更新数据库imsi数据内容
            // 写入缓存后续统一处理
            // 更新数据库imsi数据内容
            part = HomeCarrierPart(
                null,
                request.mcc, request.mnc,
                imsi.version, imsi.updateTime,
                HomePagePartForm.NET.ordinal,
                imsi.toByteArray()
            )
            part.dataProto = imsi
            homeUssdDao.insert(part)
        }
        carrierCache[request.mccMnc] = part
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

    private fun createUssdRequestBySim(sim: SimCardInfo): HomeCarrierPartRequest? {
        return if (sim.isValued())
            homeUssdDao.queryPartReq(sim.mcc, sim.mnc) ?: HomeCarrierPartRequest(null, sim.mcc, sim.mnc, 0, 0L)
        else null
    }

    suspend fun clearUssdState() {
        val stateValue = partListState.value
        val cachePart = stateValue[PART_TYPE_USSD]
        cachePart?.let {
            it.dataPart = null
            partListState.emit(stateValue.clone())
            partRefreshShare.emit(it)
        }
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