package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.PART_TYPE_TOP_UP
import com.androidx.ulife.model.PART_TYPE_USSD
import com.androidx.ulife.model.UlifeResp

@Entity(tableName = "home_carrier_part")
data class HomeCarrierPart(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "mcc") val mcc: Int,
    @ColumnInfo(name = "mnc") val mnc: Int,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long?,
    @ColumnInfo(name = "data_from") val dataFrom: Int?,
    @ColumnInfo(name = "data_byte") var dataArray: ByteArray? = null,
) {
    @Ignore
    var dataProto: Any? = null

    @Ignore
    val key: String = "$type-$mcc-$mnc"

    init {
        if (dataArray != null)
            when (type) {
                PART_TYPE_USSD -> dataProto = UlifeResp.ImsiUssdPart.parseFrom(dataArray)
                PART_TYPE_TOP_UP -> dataProto = UlifeResp.ImsiRechargePart.parseFrom(dataArray)
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HomeCarrierPart

        if (id != other.id) return false
        if (type != other.type) return false
        if (mnc != other.mnc) return false
        if (mnc != other.mnc) return false
        if (version != other.version) return false
        if (updateTime != other.updateTime) return false
//        if (dataFrom != other.dataFrom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + type
        result = 31 * result + mcc
        result = 31 * result + mnc
        result = 31 * result + version
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (dataFrom ?: 0)
        return result
    }
}