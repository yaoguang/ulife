package com.androidx.ulife.model

data class SimCardInfo(
    val subIndex: Int,
    val iccId: String,
    val imsi: Long,
    val mcc: Int,
    val mnc: Int,
) {
    val mccMnc = mcc * 1000 + mnc

    fun isValued(): Boolean = subIndex > 0
}