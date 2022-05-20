package com.androidx.ulife.model

data class ErrorInfo(
    val tag: Any? = null,
    val error: Throwable? = null,
    val others: Any? = null
)