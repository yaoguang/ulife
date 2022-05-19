package com.androidx.ulife.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [HomePagePart::class, HomeUssdPart::class], version = 1, exportSchema = false)
abstract class AppDatabaseImpl : RoomDatabase() {
    abstract fun homePageDao(): HomePageDao
    abstract fun homeUsdDao(): HomeUssdDao
}