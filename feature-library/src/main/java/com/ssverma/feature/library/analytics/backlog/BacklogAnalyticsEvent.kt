package com.ssverma.feature.library.analytics.backlog

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class BacklogAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class ChallengeClicked(
        val challengeId: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.BACKLOG_CHALLENGES
    ) : BacklogAnalyticsEvent(
        eventName = BacklogAnalyticsEventName.CHALLENGE_CLICKED,
        params = mapOf(
            BacklogAnalyticsKeys.CHALLENGE_ID to challengeId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ChallengeJoined(
        val challengeId: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.BACKLOG_CHALLENGES
    ) : BacklogAnalyticsEvent(
        eventName = BacklogAnalyticsEventName.CHALLENGE_JOINED,
        params = mapOf(
            BacklogAnalyticsKeys.CHALLENGE_ID to challengeId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class BlindspotItemClicked(
        val mediaId: Int,
        val mediaType: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.BACKLOG_CHALLENGES
    ) : BacklogAnalyticsEvent(
        eventName = BacklogAnalyticsEventName.BLINDSPOT_ITEM_CLICKED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class CustomChallengeCreated(
        val targetCount: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.BACKLOG_CHALLENGES
    ) : BacklogAnalyticsEvent(
        eventName = BacklogAnalyticsEventName.CUSTOM_CHALLENGE_CREATED,
        params = mapOf(
            BacklogAnalyticsKeys.TARGET_COUNT to targetCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
