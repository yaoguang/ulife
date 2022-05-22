package com.androidx.ulife.init

import android.content.Context
import com.androidx.ulife.simcard.SimCardManager
import com.rousetime.android_startup.AndroidStartup

class SimInitializer : AndroidStartup<SimInitializer>() {
    override fun create(context: Context): SimInitializer {
        SimCardManager.init(context)
        return this
    }

    override fun callCreateOnMainThread(): Boolean {
        return true
    }

    override fun waitOnMainThread(): Boolean {
        return true
    }
}