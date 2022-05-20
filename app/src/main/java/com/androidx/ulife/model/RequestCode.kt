package com.androidx.ulife.model

enum class RequestCode(val code: Int) {
    SUCCESS(200),
    NET_ERROR(400),
    SERVER_ERROR(500)
}