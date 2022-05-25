package com.androidx.ulife.simcard

import android.content.Context
import android.content.IntentFilter
import android.telephony.TelephonyManager
import com.androidx.ulife.model.PART_TYPE_TOP_UP
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.SimCardInfo
import com.androidx.ulife.repository.HomePageRepository
import com.androidx.ulife.utlis.getActiveSubscriptionInfoList
import com.androidx.ulife.utlis.observeBroadcast
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
object SimCardManager {
    private val NULL_SIM = SimCardInfo(-1, "", 111L, 1, 1)

    var sim1: SimCardInfo
        private set
    var sim2: SimCardInfo
        private set

    init {
        sim1 = NULL_SIM
        sim2 = NULL_SIM
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            context.observeBroadcast(IntentFilter("android.intent.action.SIM_STATE_CHANGED"), extractData = { ctx, intent ->
                intent
            })
                .debounce(1000) // 只收集最后一个事件
                .collect {
                    if (isReadySimState()) {
                        sim1 = NULL_SIM
                        sim2 = NULL_SIM
                        val list = getActiveSubscriptionInfoList()
                        list.forEach {
                            if (it.simSlotIndex == 0) {
                                sim1 = SimCardInfo(it.simSlotIndex, it.iccId, it.cardId.toLong(), it.mcc, it.mnc)
                            } else if (it.simSlotIndex == 1) {
                                sim2 = SimCardInfo(it.simSlotIndex, it.iccId, it.cardId.toLong(), it.mcc, it.mnc)
                            }
                        }
                        HomePageRepository.homePageInfo(arrayListOf(PART_TYPE_USSD, PART_TYPE_TOP_UP)).catch {
                            LogUtils.e("init_info", "sim request error", it.message)
                        }.collect()
                    } else {
                        HomePageRepository.clearImsiState()
                    }
                }
        }
    }

    fun isReadySimState(): Boolean {
        return getTelephonyManager().simState == TelephonyManager.SIM_STATE_READY
    }

    private fun getTelephonyManager(): TelephonyManager {
        return Utils.getApp().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }
}