package com.ssverma.core.billing.model

import com.android.billingclient.api.ProductDetails

data class BillingProduct(
    val id: String,
    val name: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val productType: ProductType,
    val billingPeriod: String? = null,
    val freeTrialPeriod: String? = null,
    val rawProductDetails: ProductDetails? = null
)

enum class ProductType {
    INAPP,
    SUBS
}
