package com.androidx.ulife.net

import com.androidx.ulife.dao.HomePagePartRequest
import com.androidx.ulife.model.*
import com.androidx.ulife.net.BaseUrls.API_SERVER_URI
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

    private val generalParams by lazy {
        generalParams {

        }
    }

    @Throws(Exception::class)
    fun homePageInfo(requests: List<HomePagePartRequest>): Flow<UlifeResp.QueryResponse> {
        val req = UlifeServiceGrpcKt.UlifeServiceCoroutineStub(channel)

        return req.queryUlife(queryRequest {
            general = generalParams
            requests.forEach {
                partition.add(it.toHomePartRequest())
            }
        }, Metadata())
    }
}