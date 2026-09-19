package com.ssverma.shared.analytics.community

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to

data class FirestoreAuditAnalyticsEvent(
    val queryTag: String,
    val screenName: String?,
    val docsCount: Int,
    val isFromCache: Boolean,
    val durationMs: Long
) : AnalyticsEvent {
    override val eventName: String = FirestoreAnalyticsConstants.EVENT_FIRESTORE_QUERY_AUDIT

    override val params: Map<String, AnalyticsParam> = buildList {
        add(FirestoreAnalyticsConstants.PARAM_QUERY_TAG to queryTag)
        add(FirestoreAnalyticsConstants.PARAM_DOCS_COUNT to docsCount)
        add(FirestoreAnalyticsConstants.PARAM_IS_FROM_CACHE to isFromCache)
        add(FirestoreAnalyticsConstants.PARAM_DURATION_MS to durationMs)
        if (!screenName.isNullOrBlank()) {
            add(FirestoreAnalyticsConstants.PARAM_SCREEN_NAME to screenName)
        }
    }.toMap()
}
