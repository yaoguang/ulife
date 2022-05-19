package com.androidx.ulife.base

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.androidx.ulife.app.app
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback

abstract class BaseActivity : AppCompatActivity() {
    fun requestPermissionX(callback: RequestCallback?, vararg permissions: String) {
        PermissionX.init(this)
            .permissions(listOf(*permissions))
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel")
                callback?.onResult(false, emptyList(), deniedList)
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
                callback?.onResult(false, emptyList(), deniedList)
            }
            .request(callback)
    }

    @MainThread
    inline fun <reified VM : ViewModel> androidViewModels(): Lazy<VM> =
        ViewModelLazy(VM::class,
            { viewModelStore },
            { ViewModelProvider.AndroidViewModelFactory.getInstance(app) })
}