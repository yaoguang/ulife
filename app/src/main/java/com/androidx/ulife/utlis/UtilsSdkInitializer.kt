package com.androidx.ulife.utlis

import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.Utils
import com.rousetime.android_startup.AndroidStartup

class UtilsSdkInitializer : AndroidStartup<UtilsSdkInitializer>() {
    override fun create(context: Context): UtilsSdkInitializer {
        Utils.init(context.applicationContext as Application)
        return this
    }

    override fun callCreateOnMainThread(): Boolean {
        return true
    }

    override fun waitOnMainThread(): Boolean {
        return true
    }
}