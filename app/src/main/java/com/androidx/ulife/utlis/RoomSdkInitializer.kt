package com.androidx.ulife.utlis

import android.app.Application
import android.content.Context
import com.androidx.ulife.dao.AppDatabase.init
import com.rousetime.android_startup.AndroidStartup

class RoomSdkInitializer : AndroidStartup<RoomSdkInitializer>() {
    override fun create(context: Context): RoomSdkInitializer {
        init((context.applicationContext as Application))
        return this
    }

    override fun callCreateOnMainThread(): Boolean {
        return false
    }

    override fun waitOnMainThread(): Boolean {
        return true
    }
}