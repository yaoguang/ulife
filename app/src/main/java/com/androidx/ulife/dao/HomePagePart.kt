package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_page_part")
data class HomePagePart(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "part_type") val partType: Int,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "update_time") val updateTime: Long?,
    @ColumnInfo(name = "refresh_mode") val refreshMode: Int?,
    @ColumnInfo(name = "data_from") val dataFrom: Int?,
    @ColumnInfo(name = "data_byte") val dataArray: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HomePagePart

        if (id != other.id) return false
        if (partType != other.partType) return false
        if (version != other.version) return false
        if (updateTime != other.updateTime) return false
        if (refreshMode != other.refreshMode) return false
        if (dataFrom != other.dataFrom) return false
        if (dataArray != null) {
            if (other.dataArray == null) return false
            if (!dataArray.contentEquals(other.dataArray)) return false
        } else if (other.dataArray != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + partType
        result = 31 * result + version
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (refreshMode ?: 0)
        result = 31 * result + (dataFrom ?: 0)
        result = 31 * result + (dataArray?.contentHashCode() ?: 0)
        return result
    }

//    private fun byteData(): Any {
//        return if (dataArray == null)
//            "null"
//        else {
//            UlifeResp.AdPicItem.parseFrom(dataArray)
//        }
//    }
}