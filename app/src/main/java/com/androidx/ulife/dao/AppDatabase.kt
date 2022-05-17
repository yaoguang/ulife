package com.androidx.ulife.dao

import android.app.Application
import androidx.room.Room

object AppDatabase {
    private const val NAME = "UlifeDb"

    private lateinit var application: Application
    lateinit var appDb: AppDatabaseImpl
        private set

    fun init(application: Application) {
        this.application = application

        appDb = Room.databaseBuilder(application, AppDatabaseImpl::class.java, NAME).build()
    }
}