package com.androidx.ulife.net

import com.androidx.ulife.dao.HomePagePartRequest
import com.androidx.ulife.model.*
import com.androidx.ulife.net.BaseUrls.API_SERVER_URI
import com.androidx.ulife.simcard.SimCardManager
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow

object GrpcApi {
    private val channel by lazy {
        ManagedChannelBuilder
            .forAddress(API_SERVER_URI.host, API_SERVER_URI.port)
            .usePlaintext()
            .executor(Dispatchers.IO.asExecutor())
            .build()
    }

    private lateinit var generalParams: GeneralParamsOuterClass.GeneralParams

    init {
        createGeneralParams()
    }

    private fun createGeneralParams() {
        val sim1 = SimCardManager.sim1
        val sim2 = SimCardManager.sim2
        generalParams = generalParams {
            mcc1 = sim1.mcc.toString()
            mnc1 = sim1.mnc.toString()
            imsi1 = sim1.imsi.toString()
            iccid1 = sim1.iccId
            mcc2 = sim2.mcc.toString()
            mnc2 = sim2.mnc.toString()
            imsi2 = sim2.imsi.toString()
            iccid2 = sim2.iccId
        }
    }

    @Throws(Exception::class)
    fun homePageInfo(requests: List<HomePagePartRequest>): Flow<UlifeResp.QueryResponse> {
        val req = UlifeServiceGrpcKt.UlifeServiceCoroutineStub(channel)
        createGeneralParams()
        return req.queryUlife(queryRequest {
            general = generalParams
            requests.forEach {
                partition.add(it.toHomePartRequest())
            }
        }, Metadata())
    }
}