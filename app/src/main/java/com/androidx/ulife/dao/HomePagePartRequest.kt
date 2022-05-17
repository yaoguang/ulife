package com.androidx.ulife.dao

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class HomePagePartRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name = "part_type") val partType: Int,
    @ColumnInfo(name = "version") var version: Int,
    @ColumnInfo(name = "update_time") var updateTime: Long
)