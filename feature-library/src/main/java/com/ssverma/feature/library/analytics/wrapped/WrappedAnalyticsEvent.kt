package com.ssverma.feature.library.analytics.wrapped

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class WrappedAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class YearSelected(
        val year: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_WRAPPED
    ) : WrappedAnalyticsEvent(
        eventName = WrappedAnalyticsEventName.WRAPPED_YEAR_SELECTED,
        params = mapOf(
            SharedAnalyticsKeys.YEAR to year,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class MilestoneClicked(
        val milestoneId: String,
        val isUnlocked: Boolean,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_WRAPPED
    ) : WrappedAnalyticsEvent(
        eventName = WrappedAnalyticsEventName.WRAPPED_MILESTONE_CLICKED,
        params = mapOf(
            WrappedAnalyticsKeys.MILESTONE_ID to milestoneId,
            WrappedAnalyticsKeys.IS_UNLOCKED to isUnlocked,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class StoryExportClicked(
        val year: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_WRAPPED
    ) : WrappedAnalyticsEvent(
        eventName = WrappedAnalyticsEventName.WRAPPED_STORY_EXPORTED,
        params = mapOf(
            SharedAnalyticsKeys.YEAR to year,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
