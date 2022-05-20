package com.androidx.ulife.init

import android.content.Context
import com.androidx.ulife.dao.AppDatabase
import com.androidx.ulife.dao.HomePageDao
import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.dao.HomeUssdPart
import com.androidx.ulife.model.HomePagePartForm
import com.androidx.ulife.model.RefreshMode
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.rousetime.android_startup.AndroidStartup
import com.rousetime.android_startup.Startup

class GlobalDataInitializer : AndroidStartup<GlobalDataInitializer>() {
    private var homePageDao: HomePageDao? = null

    override fun create(context: Context): GlobalDataInitializer {
        homePageDao = AppDatabase.appDb.homePageDao()
        initGlobalData()
        homePageDao?.clearOldParts()
        AppDatabase.appDb.homeUssdDao().clearOldParts()
        return this
    }

    override fun callCreateOnMainThread(): Boolean = false

    override fun waitOnMainThread(): Boolean = false

    override fun dependencies(): List<Class<out Startup<*>>> {
        return arrayListOf(UtilsSdkInitializer::class.java, RoomSdkInitializer::class.java)
    }

    private fun initGlobalData() {
        val updateGlobalVersion = SPUtils.getInstance().getInt(KEY_UPDATE_GLOBAL_DATA_VERSION, 0)
        val appVersion = AppUtils.getAppVersionCode()
        if (updateGlobalVersion < appVersion) {
            copyGlobalInfoToDatabase()
            SPUtils.getInstance().put(KEY_UPDATE_GLOBAL_DATA_VERSION, appVersion)
        }
    }

    private fun copyGlobalInfoToDatabase() {
        val localList = arrayListOf(
            HomePagePart(null, 1, 1, System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, null),
            HomePagePart(null, 2, 1, System.currentTimeMillis(), RefreshMode.ON_CREATE.ordinal, HomePagePartForm.GLOBAL.ordinal, null),
            HomePagePart(null, 4, 1, System.currentTimeMillis(), RefreshMode.ON_NEXT.ordinal, HomePagePartForm.GLOBAL.ordinal, null),
            HomePagePart(null, 8, 1, System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, null),
            HomePagePart(null, 16, 1, System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, null),
            HomePagePart(null, 32, 1, System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, null)
        )
        val ussdLocal = localList.firstOrNull { it.partType == 16 }
        if (ussdLocal != null) {
            AppDatabase.appDb.homeUssdDao().insert(
                arrayListOf(
                    HomeUssdPart(null, 400, 19, 1, 0, HomePagePartForm.GLOBAL.ordinal, null),
                    HomeUssdPart(null, 400, 20, 1, 0, HomePagePartForm.GLOBAL.ordinal, null),
                    HomeUssdPart(null, 500, 20, 1, 0, HomePagePartForm.GLOBAL.ordinal, null),
                    HomeUssdPart(null, 500, 21, 1, 0, HomePagePartForm.GLOBAL.ordinal, null)
                )
            )
        }
        homePageDao?.insert(localList)
    }

    companion object {
        private const val KEY_UPDATE_GLOBAL_DATA_VERSION = "KEY_UPDATE_GLOBAL_DATA_VERSION"
    }
}