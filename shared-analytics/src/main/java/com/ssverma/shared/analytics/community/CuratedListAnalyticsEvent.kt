package com.ssverma.shared.analytics.community

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class CuratedListAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class ListClicked(
        val listId: String,
        val listTitle: String,
        val itemsCount: Int,
        val sourceScreen: String
    ) : CuratedListAnalyticsEvent(
        eventName = CuratedListAnalyticsEventName.CURATED_LIST_CLICKED,
        params = mapOf(
            CuratedListAnalyticsKeys.LIST_ID to listId,
            CuratedListAnalyticsKeys.LIST_TITLE to listTitle,
            SharedAnalyticsKeys.ITEMS_COUNT to itemsCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ListUpvoted(
        val listId: String,
        val sourceScreen: String
    ) : CuratedListAnalyticsEvent(
        eventName = CuratedListAnalyticsEventName.CURATED_LIST_UPVOTED,
        params = mapOf(
            CuratedListAnalyticsKeys.LIST_ID to listId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ListCloned(
        val listId: String,
        val listTitle: String,
        val sourceScreen: String
    ) : CuratedListAnalyticsEvent(
        eventName = CuratedListAnalyticsEventName.CURATED_LIST_CLONED,
        params = mapOf(
            CuratedListAnalyticsKeys.LIST_ID to listId,
            CuratedListAnalyticsKeys.LIST_TITLE to listTitle,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
