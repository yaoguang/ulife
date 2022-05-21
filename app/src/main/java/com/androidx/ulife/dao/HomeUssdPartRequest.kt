package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.UlifeReq
import com.androidx.ulife.model.queryUssdPart

data class HomeUssdPartRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "mcc") val mcc: Int,
    @ColumnInfo(name = "mnc") val mnc: Int,
    @ColumnInfo(name = "version") var version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long
) {
    @Ignore
    val mccMnc: String = "$mcc-$mnc"

    fun toUssdRequest(): UlifeReq.QueryUssdPart {
        return queryUssdPart {
            version = this@HomeUssdPartRequest.version
            updateTime = this@HomeUssdPartRequest.updateTime
            mcc = this@HomeUssdPartRequest.mcc
            mnc = this@HomeUssdPartRequest.mnc
        }
    }
}