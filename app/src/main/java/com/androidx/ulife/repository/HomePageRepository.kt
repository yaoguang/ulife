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
            PART_TYPE_USSD,
            PART_TYPE_TOP_UP -> {
                val imsiListPart = part.dataPart as? HomeImsiListPart ?: return
                imsiListPart.imsi1?.let { carrierCache[it.key] = it }
                imsiListPart.imsi2?.let { carrierCache[it.key] = it }
            }
        }
    }

    suspend fun localInfo(): List<HomePagePart> {
        val parts = homePageDao.queryParts()
        parts.forEach {
            when (it.partType) {
                PART_TYPE_USSD,
                PART_TYPE_TOP_UP
                -> convertUssdLocalData(it)
            }
        }
        notifyListState(parts as ArrayList<HomePagePart>)
        return parts
    }

    private fun convertUssdLocalData(part: HomePagePart) {
        val imsiListPart = HomeImsiListPart()
        if (SimCardManager.sim1.isValued())
            imsiListPart.imsi1 = convertUssdImsiLocalData(part, SimCardManager.sim1, 1)
        if (SimCardManager.sim2.isValued())
            imsiListPart.imsi2 = convertUssdImsiLocalData(part, SimCardManager.sim2, 2)
        part.dataPart = imsiListPart
        part.dataProto = null
        part.dataArray = null
    }

    private fun convertUssdImsiLocalData(part: HomePagePart, cardInfo: SimCardInfo, simIndex: Int): HomeCarrierPart {
        var cachePart = homeUssdDao.queryPart(cardInfo.mcc, cardInfo.mnc, part.partType)
        // ???????????????????????????????????????
        if (cachePart == null) {
            cachePart = HomeCarrierPart(null, part.partType, cardInfo.mcc, cardInfo.mnc, 0, part.updateTime, HomePagePartForm.NET.ordinal, null)
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

    /**
     * ????????????????????????????????????
     */
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
            PART_TYPE_USSD,
            PART_TYPE_TOP_UP
            -> convertUssdResponseData(request, it, response)
        }
        return it
    }

    private fun convertNormalResponseData(request: HomePagePartRequest, it: UlifeResp.QueryResponse): HomePagePart {
        val part: HomePagePart
        // ??????????????????????????????????????????????????????
        if (it.dataPartCase == UlifeResp.QueryResponse.DataPartCase.DATAPART_NOT_SET) {
            // req ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            request.updateTime = it.updateTime
            homePageDao.updateReqPart(request)

            // ??????????????????
            var cachePart = partCache[request.partType]
            // ????????????????????????
            if (cachePart != null)
                cachePart.updateTime = it.updateTime

            // ?????????????????????????????????????????????????????????????????????????????????updateTime??????????????????????????????
            if (cachePart == null) {
                cachePart = homePageDao.queryPart(request.partType)
            }

            // ???????????????????????????????????????
            if (cachePart == null) {
                cachePart = HomePagePart(request.id, request.partType, request.version, request.updateTime, it.code, HomePagePartForm.NET.ordinal, it.toByteArray()).apply { dataProto = it }
            }
            part = cachePart
        } else {
            // ?????????????????????????????????cache??????????????????????????????
            part = HomePagePart(
                null,
                it.partType,
                it.version,
                it.updateTime,
                RefreshMode.ON_RESUME.ordinal,
                HomePagePartForm.NET.ordinal,
                if (it.partType == PART_TYPE_USSD || it.partType == PART_TYPE_TOP_UP) null else it.toByteArray()
            )
            part.dataProto = it
            // ???????????????
            homePageDao.insert(part)
        }
        partCache[it.partType] = part
        return part
    }

    private fun convertUssdResponseData(request: HomePagePartRequest, part: HomePagePart, response: UlifeResp.QueryResponse) {
        val imsiListPart = HomeImsiListPart()
        if (request.imsi1 != null)
            imsiListPart.imsi1 = convertUssdImsiResponseData(request.imsi1!!, response, 1)
        if (request.imsi2 != null)
            imsiListPart.imsi2 = convertUssdImsiResponseData(request.imsi2!!, response, 2)
        part.dataPart = imsiListPart
        part.dataProto = null
        part.dataArray = null
    }

    private fun convertUssdImsiResponseData(
        request: HomeCarrierPartRequest,
        response: UlifeResp.QueryResponse,
        simIndex: Int
    ): HomeCarrierPart? {
        return when (response.partType) {
            PART_TYPE_USSD -> {
                var imsiPart = if (simIndex == 1) response.ussdPart.imsi1 else response.ussdPart.imsi2
                imsiPart = if (imsiPart.dataSetList.isNullOrEmpty()) null else imsiPart
                convertUssdImsiResponseData(
                    request, response,
                    imsiPart, imsiPart?.version, response.updateTime, imsiPart?.toByteArray(), simIndex
                )
            }
            PART_TYPE_TOP_UP -> {
                var imsiPart = if (simIndex == 1) response.rechargePart.imsi1 else response.rechargePart.imsi2
                imsiPart = if (imsiPart.dataSetList.isNullOrEmpty()) null else imsiPart
                convertUssdImsiResponseData(
                    request, response,
                    imsiPart, imsiPart?.version, response.updateTime, imsiPart?.toByteArray(), simIndex
                )
            }
            else -> null
        }
    }

    private fun convertUssdImsiResponseData(
        request: HomeCarrierPartRequest,
        response: UlifeResp.QueryResponse,
        imsi: Any?, respVersion: Int?, respUpdateTime: Long?, respData: ByteArray?,
        simIndex: Int
    ): HomeCarrierPart {
        val part: HomeCarrierPart
        // ?????????????????????????????????,???????????????????????????????????????????????????????????????????????????
        if (imsi == null) {
            // ???????????????????????????
            request.updateTime = response.updateTime
            homeUssdDao.updateReqPart(request)

            // ??????????????????
            var cachePart = carrierCache[request.key]
            // ????????????????????????
            if (cachePart != null)
                cachePart.updateTime = request.updateTime

            // ?????????????????????????????????????????????????????????????????????????????????updateTime??????????????????????????????
            if (cachePart == null) {
                cachePart = homeUssdDao.queryPart(request.mcc, request.mnc, response.partType)
            }

            // ???????????????????????????????????????
            if (cachePart == null) {
                cachePart = HomeCarrierPart(request.id, response.partType, request.mcc, request.mnc, request.version, request.updateTime, HomePagePartForm.NET.ordinal, respData).apply {
                    dataProto = imsi
                }
            }
            part = cachePart
        } else {
            // ?????????????????????????????????,???????????????????????????????????????????????????????????????imsi????????????
            // ??????????????????????????????
            // ???????????????imsi????????????
            part = HomeCarrierPart(
                null,
                response.partType,
                request.mcc, request.mnc,
                respVersion!!, respUpdateTime,
                HomePagePartForm.NET.ordinal,
                respData
            )
            part.dataProto = imsi
            homeUssdDao.insert(part)
        }
        carrierCache[request.key] = part
        return part
    }

    private fun updateUssdRequest(partRequest: List<HomePagePartRequest>) {
        partRequest.forEach {
            if (it.partType == PART_TYPE_USSD ||
                it.partType == PART_TYPE_TOP_UP
            ) {
                it.imsi1 = createUssdRequestBySim(SimCardManager.sim1, it.partType)
                it.imsi2 = createUssdRequestBySim(SimCardManager.sim2, it.partType)
            }
        }
    }

    private fun createUssdRequestBySim(sim: SimCardInfo, partType: Int): HomeCarrierPartRequest? {
        return if (sim.isValued())
            homeUssdDao.queryPartReq(sim.mcc, sim.mnc, partType) ?: HomeCarrierPartRequest(null, partType, sim.mcc, sim.mnc, 0, 0L)
        else null
    }

    suspend fun clearImsiState() {
        val stateValue = partListState.value
        val cacheList = arrayListOf(stateValue[PART_TYPE_USSD], stateValue[PART_TYPE_TOP_UP])
        var notifyList = false
        cacheList.forEach {
            if (it != null) {
                it.dataPart = null
                partRefreshShare.emit(it)
                notifyList = true
            }
        }
        if (notifyList)
            partListState.emit(stateValue.clone())
    }

    /**
     * ???????????????
     */
    @Throws(Exception::class)
    suspend fun test(): Int {
        return RetrofitApi.apiService.test()
    }

    /**
     * java??????
     */
    fun testJava(callback: SuspendCallBack<Int>) {
        RetrofitApi.suspendCall({ it.apiService.test() }, callback)
    }
}