package com.androidx.ulife.api

import retrofit2.http.POST

interface ApiService {
    @POST("/test")
    suspend fun test(): Int
}