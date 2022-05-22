package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.UlifeResp
import com.androidx.ulife.model.copy

@Entity(tableName = "home_ussd_part")
data class HomeUssdPart(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "mcc") val mcc: Int,
    @ColumnInfo(name = "mnc") val mnc: Int,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long?,
    @ColumnInfo(name = "data_from") val dataFrom: Int?,
    @ColumnInfo(name = "data_byte") var dataArray: ByteArray? = null,
) {
    @Ignore
    var dataProto: UlifeResp.ImsiPart? = null

    @Ignore
    val mccMnc: String = "$mcc-$mnc"

    init {
        if (dataArray != null)
            dataProto = UlifeResp.ImsiPart.parseFrom(dataArray)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HomeUssdPart

        if (id != other.id) return false
        if (mnc != other.mnc) return false
        if (mnc != other.mnc) return false
        if (version != other.version) return false
        if (updateTime != other.updateTime) return false
//        if (dataFrom != other.dataFrom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + mcc
        result = 31 * result + mnc
        result = 31 * result + version
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (dataFrom ?: 0)
        return result
    }

    fun toUssdImsiPart(): UlifeResp.ImsiPart? {
        return dataArray?.let { UlifeResp.ImsiPart.parseFrom(it) }?.copy {
            version = this@HomeUssdPart.version
            updateTime = this@HomeUssdPart.updateTime ?: 0L
        }
    }
}