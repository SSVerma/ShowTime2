package com.ssverma.shared.analytics

object NetworkAnalyticsEventName {
    const val NETWORK_HTTP_ERROR = "network_http_error"
    const val NETWORK_FAILURE = "network_failure"
}

object NetworkAnalyticsParamKeys {
    const val ENDPOINT = "endpoint"
    const val HTTP_CODE = "http_code"
    const val ERROR_MESSAGE = "error_message"
    const val EXCEPTION_TYPE = "exception_type"
    const val DURATION_MS = "duration_ms"
}
