package com.androidx.ulife.net

import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.dao.HomePagePartRequest
import com.androidx.ulife.model.*
import com.androidx.ulife.net.BaseUrls.API_SERVER_URI
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object GrpcApi {
    private val channel by lazy {
        ManagedChannelBuilder
            .forAddress(API_SERVER_URI.host, API_SERVER_URI.port)
            .usePlaintext()
            .executor(Dispatchers.IO.asExecutor())
            .build()
    }

    private val generalParams by lazy {
        generalParams {

        }
    }

    @Throws(Exception::class)
    fun homePageInfo(requests: List<HomePagePartRequest>): Flow<Any?> {
        val req = UlifeServiceGrpcKt.UlifeServiceCoroutineStub(channel)

        return req.queryUlife(queryRequest {
            general = generalParams
            requests.forEach {
                partition.add(it.toHomePartRequest())
            }
        }, Metadata())
            .map {
                var part: HomePagePart? = null
                val dataProto: Any?
                val dataArray: ByteArray?
                when {
                    it.hasAdPicPart() -> {
                        dataProto = it.adPicPart
                        dataArray = it.adPicPart.toByteArray()
                    }
                    it.hasPinnedPart() -> {
                        dataProto = it.pinnedPart
                        dataArray = it.pinnedPart.toByteArray()
                    }
                    it.hasUssdPart() -> {
                        dataProto = it.ussdPart
                        dataArray = it.ussdPart.toByteArray()
                    }
                    it.hasTopupPart() -> {
                        dataProto = it.topupPart
                        dataArray = it.topupPart.toByteArray()
                    }
                    it.hasAdTxtPart() -> {
                        dataProto = it.adTxtPart
                        dataArray = it.adTxtPart.toByteArray()
                    }
                    else -> {
                        return@map requests.firstOrNull { req -> it.partType == req.partType }?.apply { updateTime = System.currentTimeMillis() }
                    }
                }
                if (part == null) {
                    part = HomePagePart(
                        null,
                        it.partType,
                        it.version,
                        System.currentTimeMillis(),
                        RefreshMode.ON_RESUME.ordinal,
                        HomePagePartForm.NET.ordinal,
                        dataArray
                    )
                    part.dataProto = dataProto
                }
                part
            }
    }

    @JvmStatic
    fun queryResponseDataPart(response: UlifeResp.QueryResponse): Any? {
        return when {
            response.hasAdPicPart() -> response.adPicPart
            response.hasPinnedPart() -> response.pinnedPart
            response.hasUssdPart() -> response.ussdPart
            response.hasTopupPart() -> response.topupPart
            response.hasAdTxtPart() -> response.adTxtPart
            else -> null
        }
    }

    @JvmStatic
    fun queryResponseDataPartArray(data: Any?): ByteArray? {
        if (data == null)
            return null
        return when (data) {
            is UlifeResp.AdPicPart -> data.toByteArray()
            is UlifeResp.PinnedPart -> data.toByteArray()
            is UlifeResp.UssdPart -> data.toByteArray()
            is UlifeResp.TopupPart -> data.toByteArray()
            is UlifeResp.AdTxtPart -> data.toByteArray()
            else -> null
        }
    }
}