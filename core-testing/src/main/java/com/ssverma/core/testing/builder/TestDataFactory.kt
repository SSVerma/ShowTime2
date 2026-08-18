package com.ssverma.core.testing.builder

import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.ProductType

object TestDataFactory {

    fun createBillingProduct(
        id: String = BillingConstants.SKU_PRO_LIFETIME,
        name: String = "ShowTime Pro (Lifetime)",
        description: String = "One-time purchase for permanent ad-free access.",
        formattedPrice: String = "$4.99",
        priceAmountMicros: Long = 4990000,
        priceCurrencyCode: String = "USD",
        productType: ProductType = ProductType.INAPP
    ): BillingProduct = BillingProduct(
        id = id,
        name = name,
        description = description,
        formattedPrice = formattedPrice,
        priceAmountMicros = priceAmountMicros,
        priceCurrencyCode = priceCurrencyCode,
        productType = productType
    )
}
