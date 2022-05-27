package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.PART_TYPE_TOP_UP
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.UlifeReq
import com.androidx.ulife.model.queryPart
import com.androidx.ulife.simcard.SimCardManager

data class HomePagePartRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "part_type") val partType: Int,
    @ColumnInfo(name = "version") var version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long
) {
    @Ignore
    var imsi1: HomeCarrierPartRequest? = null

    @Ignore
    var imsi2: HomeCarrierPartRequest? = null

    fun toHomePartRequest(): UlifeReq.QueryPart {
        return queryPart {
            version = this@HomePagePartRequest.version
            updateTime = this@HomePagePartRequest.updateTime
            partType = this@HomePagePartRequest.partType
            if (partType == PART_TYPE_USSD || partType == PART_TYPE_TOP_UP) {
                imsi1?.toSimPartRequest(SimCardManager.sim1)?.let { simPart1 = it }
                imsi2?.toSimPartRequest(SimCardManager.sim1)?.let { simPart2 = it }
            }
        }
    }
}