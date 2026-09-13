package com.ssverma.core.testing.builder

import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.ProductType

object TestDataFactory {

    fun createBillingProduct(
        id: String = BillingConstants.SKU_PRO_YEARLY,
        name: String = "ShowTime Pro (Yearly)",
        description: String = "Annual auto-renewing Pro subscription.",
        formattedPrice: String = "$11.99/yr",
        priceAmountMicros: Long = 11990000,
        priceCurrencyCode: String = "USD",
        productType: ProductType = ProductType.SUBS,
        billingPeriod: String? = "P1Y"
    ): BillingProduct = BillingProduct(
        id = id,
        name = name,
        description = description,
        formattedPrice = formattedPrice,
        priceAmountMicros = priceAmountMicros,
        priceCurrencyCode = priceCurrencyCode,
        productType = productType,
        billingPeriod = billingPeriod
    )
}
