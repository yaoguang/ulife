@file:Suppress("UNREACHABLE_CODE")

package com.androidx.ulife.utlis

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.Utils

/**
 * sim卡相关工具类
 */

private fun getTelephonyManager(): TelephonyManager {
    return Utils.getApp().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
}

@SuppressLint("NewApi")
private fun getSubscriptionManager(): SubscriptionManager {
    return Utils.getApp()
        .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
}

@SuppressLint("HardwareIds")
private fun getAndroidId(): String {
    return runCatching {
        Settings.Secure.getString(Utils.getApp().contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }.getOrDefault("")
}

/**
 * 检查权限
 * @return true 同意
 * @return false 不同意
 */
private fun checkSelfPermission(permission: String) = ActivityCompat.checkSelfPermission(
    Utils.getApp(), permission
) == PackageManager.PERMISSION_GRANTED

@SuppressLint("NewApi")
fun getSimCardCount(): Int {
    return if (!isHaveSimCard()) 0 else {
        if (!checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) return 0
        if (SystemConfig.isSupportVirtualSim()) {
            return getActiveSubscriptionInfoList().size
        }
        getSubscriptionManager().activeSubscriptionInfoCount
    }
}

fun isReadySimState(): Boolean {
    return getTelephonyManager().simState == SIM_STATE_READY
}

private fun isHaveSimCard(): Boolean {
    return when (getTelephonyManager().simState) {
        SIM_STATE_ABSENT -> false
        SIM_STATE_UNKNOWN -> false
        else -> true
    }
}

@SuppressLint("NewApi")
fun getActiveSubscriptionInfo(subId: Int): SubscriptionInfo? {
    if (!checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) return null
    return getSubscriptionManager().getActiveSubscriptionInfo(subId)
}

@SuppressLint("NewApi")
fun getActiveSubscriptionInfoForSimSlotIndex(slotIndex: Int): SubscriptionInfo? {
    if (!checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) return null
    return getSubscriptionManager().getActiveSubscriptionInfoForSimSlotIndex(slotIndex)
}

@SuppressLint("NewApi")
fun getActiveSubscriptionInfoList(): List<SubscriptionInfo> {
    if (!checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) return emptyList()
    return getSubscriptionManager().activeSubscriptionInfoList ?: emptyList()
    if (getSubscriptionManager().activeSubscriptionInfoList != null)
        return getSubscriptionManager().activeSubscriptionInfoList.filter {
            !SystemConfig.isVirtualSim(it.simSlotIndex)
        }
    return emptyList()
}

@SuppressLint("NewApi")
fun getSubId(subscriptionInfo: SubscriptionInfo): Int {
    return subscriptionInfo.subscriptionId
}

@SuppressLint("NewApi")
fun getIccId(subscriptionInfo: SubscriptionInfo): String {
    return subscriptionInfo.iccId ?: "" /* ICCID */
}