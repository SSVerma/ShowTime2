package com.ssverma.core.ads.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam

/**
 * Maps AdMob lifecycle events into your existing Analytics framework.
 */
class AdAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent
