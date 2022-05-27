package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.SimCardInfo
import com.androidx.ulife.model.UlifeReq
import com.androidx.ulife.model.querySimPart

data class HomeCarrierPartRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "mcc") val mcc: Int,
    @ColumnInfo(name = "mnc") val mnc: Int,
    @ColumnInfo(name = "version") var version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long
) {
    @Ignore
    val key: String = "$type-$mcc-$mnc"

    fun toSimPartRequest(sim: SimCardInfo): UlifeReq.QuerySimPart {
        return querySimPart {
            version = this@HomeCarrierPartRequest.version
            updateTime = this@HomeCarrierPartRequest.updateTime
            imsi = sim.imsi.toString()
        }
    }
}