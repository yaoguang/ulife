package com.androidx.ulife.net

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Looper
import com.androidx.ulife.dao.HomePagePartRequest
import com.androidx.ulife.model.*
import com.androidx.ulife.net.BaseUrls.API_SERVER_URI
import com.androidx.ulife.server.UlifeServiceGrpcKt
import com.androidx.ulife.simcard.SimCardManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.Utils
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
        if (!Looper.getMainLooper().isCurrentThread) {
            createGeneralParams()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                createGeneralParams()
            }
        }
    }

    private fun createGeneralParams() {
        val sim1 = SimCardManager.sim1
        val sim2 = SimCardManager.sim2
        val local = LanguageUtils.getSystemLanguage()

        generalParams = generalParams {
            deviceId = "1" // TODO deviceId 设备id
            sysId = "1" // TODO 系统id，例如ANDROID ID
            gaid = "1" // TODO google 广告ID
            countryCode = local?.country ?: ""
            language = local?.language ?: ""
            iccid1 = sim1.iccId
            iccid2 = sim2.iccId
            mcc1 = sim1.mcc.toString()
            mcc2 = sim2.mcc.toString()
            mnc1 = sim1.mnc.toString()
            mnc2 = sim2.mnc.toString()
            imsi1 = sim1.imsi.toString()
            imsi2 = sim2.imsi.toString()
            defaultSim = 1
            chId = "1" // TODO 渠道号
            os = "1" // TODO 系统类型,例如 hios
            appVersion = AppUtils.getAppVersionName()
            networkType = networkType()
            model = DeviceUtils.getModel()
            brand = Build.BRAND ?: ""
            manufacturer = Build.MANUFACTURER ?: ""
        }
    }

    fun homePageInfo(requests: List<HomePagePartRequest>): Flow<UlifeResp.QueryResponse> {
        val req = UlifeServiceGrpcKt.UlifeServiceCoroutineStub(channel)
        refreshGeneralParams()
        return req.queryUlife(queryRequest {
            general = generalParams
            requests.forEach {
                partition.add(it.toHomePartRequest())
            }
        }, Metadata())
    }

    fun account(): Flow<Account.User> {
        val req = UserControllerGrpcKt.UserControllerCoroutineStub(channel)
        refreshGeneralParams()
        return req.list(userListRequest { }, Metadata())
    }

    private fun refreshGeneralParams() {
        generalParams = generalParams.copy {
            networkType = networkType()
        }
    }

    private fun refreshGeneralParams(block: GeneralParamsKt.Dsl.() -> Unit) {
        generalParams = generalParams.copy(block)
    }

    fun refreshGeneralParamsSmi() {
        val sim1 = SimCardManager.sim1
        val sim2 = SimCardManager.sim2
        generalParams = generalParams.copy {
            iccid1 = sim1.iccId
            iccid2 = sim2.iccId
            mcc1 = sim1.mcc.toString()
            mcc2 = sim2.mcc.toString()
            mnc1 = sim1.mnc.toString()
            mnc2 = sim2.mnc.toString()
            imsi1 = sim1.imsi.toString()
            imsi2 = sim2.imsi.toString()
        }
    }

    /**
     * 0：无网络 1：移动网络 2：Wi-F
     */
    @Suppress("DEPRECATION")
    fun networkType(): Int {
        val cm = Utils.getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return 0
        val netInfo = cm.activeNetworkInfo ?: return 0
        if (netInfo.isConnected) {
            return if (netInfo.type == ConnectivityManager.TYPE_WIFI) 2 else 1
        }
        return 0
    }
}