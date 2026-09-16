package com.ssverma.feature.library.analytics.diary

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class DiaryAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class EntryLogged(
        val mediaId: Int,
        val mediaType: String,
        val rating: Float,
        val isRewatch: Boolean,
        val hasReview: Boolean,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_DIARY
    ) : DiaryAnalyticsEvent(
        eventName = DiaryAnalyticsEventName.DIARY_ENTRY_LOGGED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.RATING to rating,
            DiaryAnalyticsKeys.IS_REWATCH to isRewatch,
            DiaryAnalyticsKeys.HAS_REVIEW to hasReview,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class EntryDeleted(
        val mediaId: Int,
        val mediaType: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_DIARY
    ) : DiaryAnalyticsEvent(
        eventName = DiaryAnalyticsEventName.DIARY_ENTRY_DELETED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ReviewShared(
        val mediaId: Int,
        val mediaType: String,
        val rating: Float,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_DIARY
    ) : DiaryAnalyticsEvent(
        eventName = DiaryAnalyticsEventName.DIARY_REVIEW_SHARED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.RATING to rating,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class FilterChanged(
        val year: String?,
        val rating: String?,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_DIARY
    ) : DiaryAnalyticsEvent(
        eventName = DiaryAnalyticsEventName.DIARY_FILTER_CHANGED,
        params = mapOf(
            SharedAnalyticsKeys.YEAR to (year ?: "all"),
            SharedAnalyticsKeys.RATING to (rating ?: "all"),
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ItemClicked(
        val mediaId: Int,
        val mediaType: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_DIARY
    ) : DiaryAnalyticsEvent(
        eventName = DiaryAnalyticsEventName.DIARY_ITEM_CLICKED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
