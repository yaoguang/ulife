package com.androidx.ulife.net

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppClient {
    private const val DEFAULT_TIMEOUT = 10L

    val retrofit by lazy { get() }

    fun get(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) //错误重链
            .build()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BaseUrls.API_SERVER)
            .client(okHttpClient)
            .build()
    }
}