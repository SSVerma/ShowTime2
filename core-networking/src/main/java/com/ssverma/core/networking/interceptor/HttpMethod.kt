package com.ssverma.core.networking.interceptor

/**
 * Standard HTTP method constants to avoid magic strings in interceptors.
 */
internal object HttpMethod {
    const val GET = "GET"
    const val POST = "POST"
    const val PUT = "PUT"
    const val DELETE = "DELETE"
    const val PATCH = "PATCH"
}
