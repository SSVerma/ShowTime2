package com.ssverma.feature.filter.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class FilterAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class MediaTypeSwitched(
        val mediaType: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.MEDIA_TYPE_SWITCHED,
        params = mapOf(
            FilterAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class VibeSelected(
        val vibeId: String,
        val vibeName: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.VIBE_SELECTED,
        params = mapOf(
            FilterAnalyticsKeys.VIBE_ID to vibeId,
            FilterAnalyticsKeys.VIBE_NAME to vibeName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ProviderToggled(
        val providerId: Int,
        val providerName: String,
        val isSelected: Boolean,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.PROVIDER_TOGGLED,
        params = mapOf(
            FilterAnalyticsKeys.PROVIDER_ID to providerId,
            FilterAnalyticsKeys.PROVIDER_NAME to providerName,
            FilterAnalyticsKeys.IS_SELECTED to isSelected,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class DecadeSelected(
        val decade: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.DECADE_SELECTED,
        params = mapOf(
            FilterAnalyticsKeys.DECADE to decade,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class StudioClicked(
        val studioId: Int,
        val studioName: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.STUDIO_CLICKED,
        params = mapOf(
            FilterAnalyticsKeys.STUDIO_ID to studioId,
            FilterAnalyticsKeys.STUDIO_NAME to studioName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RouletteOpened(
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.ROULETTE_OPENED,
        params = mapOf(
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RouletteSpun(
        val itemId: Int,
        val itemTitle: String,
        val mediaType: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.ROULETTE_SPUN,
        params = mapOf(
            FilterAnalyticsKeys.ITEM_ID to itemId,
            FilterAnalyticsKeys.ITEM_TITLE to itemTitle,
            FilterAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RouletteAccepted(
        val itemId: Int,
        val mediaType: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.ROULETTE_ACCEPTED,
        params = mapOf(
            FilterAnalyticsKeys.ITEM_ID to itemId,
            FilterAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ItemClicked(
        val itemId: Int,
        val mediaType: String,
        val section: String = FilterAnalyticsValues.SECTION_DISCOVERY_FEED,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.ITEM_CLICKED,
        params = mapOf(
            FilterAnalyticsKeys.ITEM_ID to itemId,
            FilterAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class FilterSheetApplied(
        val sortBy: String,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.FILTER_SHEET_APPLIED,
        params = mapOf(
            FilterAnalyticsKeys.SORT_BY to sortBy,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class LayoutToggled(
        val isGrid: Boolean,
        val sourceScreen: String = FilterAnalyticsScreenName.UNIVERSAL_DISCOVER
    ) : FilterAnalyticsEvent(
        eventName = FilterAnalyticsEventName.LAYOUT_TOGGLED,
        params = mapOf(
            FilterAnalyticsKeys.IS_GRID to isGrid,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
