package com.ssverma.core.analytics

interface AnalyticsEvent {
    val eventName: String
    val params: Map<String, AnalyticsParam> get() = emptyMap()
}
