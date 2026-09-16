package com.ssverma.shared.analytics.ads

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to

sealed class FeatureGateAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class GateViewed(
        val passKey: String,
        val style: String
    ) : FeatureGateAnalyticsEvent(
        eventName = FeatureGateAnalyticsEventName.FEATURE_GATE_VIEWED,
        params = mapOf(
            FeatureGateAnalyticsKeys.PASS_KEY to passKey,
            FeatureGateAnalyticsKeys.STYLE to style
        )
    )

    data class WatchAdClicked(
        val passKey: String
    ) : FeatureGateAnalyticsEvent(
        eventName = FeatureGateAnalyticsEventName.FEATURE_GATE_WATCH_AD_CLICKED,
        params = mapOf(
            FeatureGateAnalyticsKeys.PASS_KEY to passKey
        )
    )

    data class UpgradeProClicked(
        val passKey: String
    ) : FeatureGateAnalyticsEvent(
        eventName = FeatureGateAnalyticsEventName.FEATURE_GATE_UPGRADE_PRO_CLICKED,
        params = mapOf(
            FeatureGateAnalyticsKeys.PASS_KEY to passKey
        )
    )

    data class GateDismissed(
        val passKey: String
    ) : FeatureGateAnalyticsEvent(
        eventName = FeatureGateAnalyticsEventName.FEATURE_GATE_DISMISSED,
        params = mapOf(
            FeatureGateAnalyticsKeys.PASS_KEY to passKey
        )
    )
}
