package com.androidx.ulife.net

import com.androidx.ulife.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RetrofitApi {
    val apiService: ApiService by lazy { AppClient.retrofit.create(ApiService::class.java) }

    fun <T> suspendCall(call: SuspendCall<T>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = call.call()
                withContext(Dispatchers.Main) {
                    call.onResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    call.onFailure(e)
                }
            }
        }
    }

    fun <T> suspendCall(call: suspend (RetrofitApi) -> T, callback: SuspendCallBack<T>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = call.invoke(this@RetrofitApi)
                withContext(Dispatchers.Main) {
                    callback.onResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e)
                }
            }
        }
    }
}