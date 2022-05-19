package com.androidx.ulife.app

import android.app.Application

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initApplicationAbout(this)
    }
}