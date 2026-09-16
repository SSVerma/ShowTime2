package com.ssverma.feature.library.analytics.receipt

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.library.analytics.LibraryAnalyticsScreenName
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class CinemaReceiptAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class PeriodSelected(
        val period: String,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_RECEIPT
    ) : CinemaReceiptAnalyticsEvent(
        eventName = CinemaReceiptAnalyticsEventName.RECEIPT_PERIOD_SELECTED,
        params = mapOf(
            CinemaReceiptAnalyticsKeys.PERIOD to period,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ReceiptShared(
        val period: String,
        val itemCount: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_RECEIPT
    ) : CinemaReceiptAnalyticsEvent(
        eventName = CinemaReceiptAnalyticsEventName.RECEIPT_SHARED,
        params = mapOf(
            CinemaReceiptAnalyticsKeys.PERIOD to period,
            SharedAnalyticsKeys.ITEM_COUNT to itemCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ReceiptSaved(
        val period: String,
        val itemCount: Int,
        val sourceScreen: String = LibraryAnalyticsScreenName.CINEMA_RECEIPT
    ) : CinemaReceiptAnalyticsEvent(
        eventName = CinemaReceiptAnalyticsEventName.RECEIPT_SAVED,
        params = mapOf(
            CinemaReceiptAnalyticsKeys.PERIOD to period,
            SharedAnalyticsKeys.ITEM_COUNT to itemCount,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
