package com.ssverma.shared.analytics.radar

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to

import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class ReleaseRadarAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class TheatricalAlertTriggered(
        val mediaId: Int,
        val title: String
    ) : ReleaseRadarAnalyticsEvent(
        eventName = ReleaseRadarAnalyticsEventName.RELEASE_RADAR_THEATRICAL_ALERT,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TITLE to title
        )
    )

    data class StreamingAlertTriggered(
        val mediaId: Int,
        val title: String,
        val providers: String
    ) : ReleaseRadarAnalyticsEvent(
        eventName = ReleaseRadarAnalyticsEventName.RELEASE_RADAR_STREAMING_ALERT,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TITLE to title,
            ReleaseRadarAnalyticsKeys.PROVIDERS to providers
        )
    )

    data class SyncCompleted(
        val theatricalNotifiedCount: Int,
        val streamingCheckedCount: Int,
        val streamingNotifiedCount: Int,
        val success: Boolean
    ) : ReleaseRadarAnalyticsEvent(
        eventName = ReleaseRadarAnalyticsEventName.RELEASE_RADAR_SYNC_COMPLETED,
        params = mapOf(
            ReleaseRadarAnalyticsKeys.THEATRICAL_NOTIFIED_COUNT to theatricalNotifiedCount,
            ReleaseRadarAnalyticsKeys.STREAMING_CHECKED_COUNT to streamingCheckedCount,
            ReleaseRadarAnalyticsKeys.STREAMING_NOTIFIED_COUNT to streamingNotifiedCount,
            SharedAnalyticsKeys.SUCCESS to success
        )
    )
}
