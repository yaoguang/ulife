package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.androidx.ulife.model.*

@Entity(tableName = "home_page_part")
data class HomePagePart(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @ColumnInfo(name = "part_type") val partType: Int,
    @ColumnInfo(name = "version") val version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long?,
    @ColumnInfo(name = "refresh_mode") val refreshMode: Int?,
    @ColumnInfo(name = "data_from") val dataFrom: Int?,
    @ColumnInfo(name = "data_byte") var dataArray: ByteArray? = null,
) {
    @Ignore
    var dataProto: UlifeResp.QueryResponse? = null

    @Ignore
    var dataPart: Any? = null

    init {
        if (dataArray != null)
            dataProto = UlifeResp.QueryResponse.parseFrom(dataArray)
    }

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

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + partType
        result = 31 * result + version
        result = 31 * result + (updateTime?.hashCode() ?: 0)
        result = 31 * result + (refreshMode ?: 0)
        result = 31 * result + (dataFrom ?: 0)
        return result
    }

    fun toResponse(): UlifeResp.QueryResponse? {
        if (dataArray == null)
            return null
        return queryResponse {
            partType = this@HomePagePart.partType
            version = this@HomePagePart.version
            updateTime = this@HomePagePart.updateTime ?: 0L

            when (partType) {
                PART_TYPE_DIAMOND -> pinnedPart = UlifeResp.PinnedPart.parseFrom(dataArray)
                PART_TYPE_TEXT_BANNER -> adTxtPart = UlifeResp.AdTxtPart.parseFrom(dataArray)
                PART_TYPE_IMG_BANNER -> adPicPart = UlifeResp.AdPicPart.parseFrom(dataArray)
                PART_TYPE_TOP_UP -> topupPart = UlifeResp.TopupPart.parseFrom(dataArray)
                PART_TYPE_USSD -> ussdPart = UlifeResp.UssdPart.parseFrom(dataArray)
            }
        }
    }
}