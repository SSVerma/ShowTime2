package com.ssverma.feature.payment.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class PaymentAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class PaywallViewed(
        val sourceScreen: String = PaymentAnalyticsScreenName.PRO_PAYWALL
    ) : PaymentAnalyticsEvent(
        eventName = PaymentAnalyticsEventName.PRO_PAYWALL_VIEWED,
        params = mapOf(
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class PurchaseInitiated(
        val sku: String,
        val price: String? = null,
        val sourceScreen: String = PaymentAnalyticsScreenName.PRO_PAYWALL
    ) : PaymentAnalyticsEvent(
        eventName = PaymentAnalyticsEventName.PRO_PURCHASE_INITIATED,
        params = mutableMapOf<String, AnalyticsParam>().apply {
            putAll(
                listOfNotNull(
                    PaymentAnalyticsKeys.SKU to sku,
                    price?.let { PaymentAnalyticsKeys.PRICE to it },
                    SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
                )
            )
        }
    )

    data class PurchaseCompleted(
        val sku: String? = null,
        val sourceScreen: String = PaymentAnalyticsScreenName.PRO_PAYWALL
    ) : PaymentAnalyticsEvent(
        eventName = PaymentAnalyticsEventName.PRO_PURCHASE_COMPLETED,
        params = mutableMapOf<String, AnalyticsParam>().apply {
            putAll(
                listOfNotNull(
                    sku?.let { PaymentAnalyticsKeys.SKU to it },
                    SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
                )
            )
        }
    )

    data class RestorePurchases(
        val success: Boolean,
        val sourceScreen: String = PaymentAnalyticsScreenName.PRO_PAYWALL
    ) : PaymentAnalyticsEvent(
        eventName = PaymentAnalyticsEventName.PRO_RESTORE_PURCHASES,
        params = mapOf(
            SharedAnalyticsKeys.SUCCESS to success,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
