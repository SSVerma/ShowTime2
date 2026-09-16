package com.ssverma.feature.library.analytics.taste

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class TasteProfileAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class ArchetypeViewed(
        val archetypeName: String,
        val topGenre: String?,
        val sourceScreen: String = LibraryAnalyticsScreenName.TASTE_PROFILE
    ) : TasteProfileAnalyticsEvent(
        eventName = TasteProfileAnalyticsEventName.TASTE_ARCHETYPE_VIEWED,
        params = mapOf(
            TasteProfileAnalyticsKeys.ARCHETYPE_NAME to archetypeName,
            TasteProfileAnalyticsKeys.TOP_GENRE to (topGenre ?: "none"),
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class TopGenreClicked(
        val genreName: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.TASTE_PROFILE
    ) : TasteProfileAnalyticsEvent(
        eventName = TasteProfileAnalyticsEventName.TASTE_TOP_GENRE_CLICKED,
        params = mapOf(
            TasteProfileAnalyticsKeys.GENRE_NAME to genreName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RecommendationClicked(
        val mediaId: Int,
        val mediaType: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.TASTE_PROFILE
    ) : TasteProfileAnalyticsEvent(
        eventName = TasteProfileAnalyticsEventName.TASTE_RECOMMENDATION_CLICKED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ProfileShared(
        val archetypeName: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.TASTE_PROFILE
    ) : TasteProfileAnalyticsEvent(
        eventName = TasteProfileAnalyticsEventName.TASTE_PROFILE_SHARED,
        params = mapOf(
            TasteProfileAnalyticsKeys.ARCHETYPE_NAME to archetypeName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
