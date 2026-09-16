package com.ssverma.feature.library.analytics.share

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class SecretListAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class ListLoaded(
        val shareCode: String,
        val itemsCount: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.SECRET_SHARED_LIST
    ) : SecretListAnalyticsEvent(
        eventName = SecretListAnalyticsEventName.SECRET_LIST_LOADED,
        params = mapOf(
            SecretListAnalyticsKeys.SHARE_CODE to shareCode,
            SharedAnalyticsKeys.ITEMS_COUNT to itemsCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class CodeShared(
        val shareCode: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.SECRET_SHARED_LIST
    ) : SecretListAnalyticsEvent(
        eventName = SecretListAnalyticsEventName.SECRET_LIST_CODE_SHARED,
        params = mapOf(
            SecretListAnalyticsKeys.SHARE_CODE to shareCode,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ListCloned(
        val shareCode: String,
        val itemsCount: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.SECRET_SHARED_LIST
    ) : SecretListAnalyticsEvent(
        eventName = SecretListAnalyticsEventName.SECRET_LIST_CLONED,
        params = mapOf(
            SecretListAnalyticsKeys.SHARE_CODE to shareCode,
            SharedAnalyticsKeys.ITEMS_COUNT to itemsCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ItemClicked(
        val mediaId: Int,
        val mediaType: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.SECRET_SHARED_LIST
    ) : SecretListAnalyticsEvent(
        eventName = SecretListAnalyticsEventName.SECRET_LIST_ITEM_CLICKED,
        params = mapOf(
            SharedAnalyticsKeys.MEDIA_ID to mediaId,
            SharedAnalyticsKeys.MEDIA_TYPE to mediaType,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
