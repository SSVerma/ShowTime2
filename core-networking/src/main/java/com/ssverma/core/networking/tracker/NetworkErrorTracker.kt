package com.ssverma.core.networking.tracker

interface NetworkErrorTracker {
    fun trackHttpError(
        endpoint: String,
        httpCode: Int,
        message: String?,
        durationMs: Long
    )

    fun trackNetworkException(
        endpoint: String,
        throwable: Throwable,
        durationMs: Long
    )
}
