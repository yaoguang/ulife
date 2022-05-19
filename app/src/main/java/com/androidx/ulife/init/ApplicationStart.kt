package com.androidx.ulife.init

import android.app.Application
import android.content.Context
import com.androidx.ulife.app.initApplicationAbout
import com.rousetime.android_startup.AndroidStartup

class ApplicationStart : AndroidStartup<ApplicationStart>() {

    override fun create(context: Context): ApplicationStart {
        initApplicationAbout(context.applicationContext as Application)
        return this
    }

    override fun callCreateOnMainThread(): Boolean {
        return true
    }

    override fun waitOnMainThread(): Boolean {
        return true
    }
}