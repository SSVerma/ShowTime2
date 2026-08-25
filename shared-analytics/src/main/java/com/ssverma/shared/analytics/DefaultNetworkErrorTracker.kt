package com.ssverma.shared.analytics

import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.NumberParam
import com.ssverma.core.analytics.StringParam
import com.ssverma.core.networking.tracker.NetworkErrorTracker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNetworkErrorTracker @Inject constructor(
    private val analytics: Analytics
) : NetworkErrorTracker {

    override fun trackHttpError(
        endpoint: String,
        httpCode: Int,
        message: String?,
        durationMs: Long
    ) {
        analytics.logEvent(
            object : AnalyticsEvent {
                override val eventName: String = NetworkAnalyticsEventName.NETWORK_HTTP_ERROR
                override val params: Map<String, AnalyticsParam> = mapOf(
                    NetworkAnalyticsParamKeys.ENDPOINT to StringParam(endpoint),
                    NetworkAnalyticsParamKeys.HTTP_CODE to NumberParam(httpCode),
                    NetworkAnalyticsParamKeys.ERROR_MESSAGE to StringParam(message.orEmpty()),
                    NetworkAnalyticsParamKeys.DURATION_MS to NumberParam(durationMs)
                )
            }
        )
    }

    override fun trackNetworkException(
        endpoint: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        analytics.logEvent(
            object : AnalyticsEvent {
                override val eventName: String = NetworkAnalyticsEventName.NETWORK_FAILURE
                override val params: Map<String, AnalyticsParam> = mapOf(
                    NetworkAnalyticsParamKeys.ENDPOINT to StringParam(endpoint),
                    NetworkAnalyticsParamKeys.EXCEPTION_TYPE to StringParam(throwable::class.java.simpleName),
                    NetworkAnalyticsParamKeys.ERROR_MESSAGE to StringParam(throwable.message.orEmpty()),
                    NetworkAnalyticsParamKeys.DURATION_MS to NumberParam(durationMs)
                )
            }
        )
    }
}
