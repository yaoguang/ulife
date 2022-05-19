package com.androidx.ulife.net

interface SuspendCall<T> {
    suspend fun call(): T

    fun onResponse(response: T)

    fun onFailure(t: Throwable?)
}