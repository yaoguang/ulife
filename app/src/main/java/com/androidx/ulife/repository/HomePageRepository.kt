package com.androidx.ulife.repository

import android.util.SparseArray
import androidx.core.util.containsKey
import com.androidx.ulife.dao.*
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.SimCardInfo
import com.androidx.ulife.net.GrpcApi
import com.androidx.ulife.net.RetrofitApi
import com.androidx.ulife.net.SuspendCallBack
import com.androidx.ulife.simcard.SimCardManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object HomePageRepository {
    private val homePageDao by lazy { AppDatabase.appDb.homePageDao() }
    private val homeUssdDao by lazy { AppDatabase.appDb.homeUsdDao() }

    private val homePagePartCache = SparseArray<HomePagePart>()
    private var homeUssdPartCache: HomePagePart? = null

    fun homePageInfo(): Flow<HomePagePart?> {
        val partRequest = homePageDao.queryPartReqList()
        val ussdRequest = createUssdHomePartRequest()
        (partRequest as ArrayList).add(ussdRequest)
        return GrpcApi.homePageInfo(partRequest)
            .map { convertResponseData(it) }
            .catch { }
    }

    fun homePageInfo(reqParts: List<Int>): Flow<HomePagePart?> {
        val requests = homePageDao.queryPartReqList(reqParts)
        val partRequest = reqParts.map { requests.firstOrNull { req -> req.partType == it } ?: HomePagePartRequest(null, it, 0, 0L) }
        return GrpcApi.homePageInfo(partRequest)
            .map { convertResponseData(it) }
    }

    private fun convertResponseData(it: Any?): HomePagePart? {
        return when (it) {
            // 更新数据
            is HomePagePart -> {
                updateResponseData(it)
                it
            }
            // 无需刷新数据，更新请求时间
            is HomePagePartRequest -> {
                val part = readCachePart(it)
                updateResponseData(it, part)
                part
            }
            else -> null
        }
    }

    //TODO 区分ussd并保存数据库和缓存数据
    private fun updateResponseData(it: HomePagePart) {
        if (it.partType == PART_TYPE_USSD) {
            homeUssdPartCache = it
        } else {
            homePagePartCache[it.partType] = it
            homePageDao.insert(it)
        }
    }

    // TODO 区分ussd并保存数据库和缓存数据
    private fun updateResponseData(partRequest: HomePagePartRequest?, part: HomePagePart?) {
        if (partRequest == null)
            return
        partRequest.updateTime = System.currentTimeMillis()
        homePageDao.updateReqPart(partRequest)
        if (part != null && !homePagePartCache.containsKey(partRequest.partType)) {
            homePagePartCache[partRequest.partType] = part
        }
    }

    // TODO 区分ussd并返回对应的可用 ussd 展示数据
    private fun readCachePart(partRequest: HomePagePartRequest?): HomePagePart? {
        if (partRequest == null)
            return null
        val result: HomePagePart?
        if (!homePagePartCache.containsKey(partRequest.partType)) {
            result = homePageDao.queryPart(partRequest.partType)
        } else
            result = homePagePartCache[partRequest.partType]
        return result
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

    private fun readCachePartAndUpdateDao(partRequest: HomePagePartRequest?): HomePagePart? {
        if (partRequest == null)
            return null
        partRequest.updateTime = System.currentTimeMillis()
        homePageDao.updateReqPart(partRequest)
        val result: HomePagePart?
        if (!homePagePartCache.containsKey(partRequest.partType)) {
            result = homePageDao.queryPart(partRequest.partType)
            homePagePartCache[partRequest.partType] = result
        } else
            result = homePagePartCache[partRequest.partType]
        return result
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