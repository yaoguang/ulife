package com.androidx.ulife.net

interface SuspendCallBack<T> {
    fun onResponse(response: T)

    fun onFailure(t: Throwable?)
}