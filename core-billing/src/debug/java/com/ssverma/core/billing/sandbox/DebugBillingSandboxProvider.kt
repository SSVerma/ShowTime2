package com.ssverma.core.billing.sandbox

import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.ProductType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugBillingSandboxProvider @Inject constructor() : BillingSandboxProvider {

    override fun isSandboxEnabled(): Boolean = true

    override fun getSandboxProducts(): List<BillingProduct> {
        return listOf(
            BillingProduct(
                id = BillingConstants.SKU_PRO_YEARLY,
                name = "ShowTime Pro (Yearly)",
                description = "Annual auto-renewing Pro subscription.",
                formattedPrice = "$11.99/yr",
                priceAmountMicros = 11990000L,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                billingPeriod = "P1Y",
                rawProductDetails = null
            ),
            BillingProduct(
                id = BillingConstants.SKU_PRO_SIX_MONTHS,
                name = "ShowTime Pro (6 Months)",
                description = "6-month auto-renewing Pro subscription.",
                formattedPrice = "$7.99/6mo",
                priceAmountMicros = 7990000L,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                billingPeriod = "P6M",
                rawProductDetails = null
            ),
            BillingProduct(
                id = BillingConstants.SKU_PRO_THREE_MONTHS,
                name = "ShowTime Pro (3 Months)",
                description = "3-month auto-renewing Pro subscription.",
                formattedPrice = "$4.99/3mo",
                priceAmountMicros = 4990000L,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                billingPeriod = "P3M",
                rawProductDetails = null
            ),
            BillingProduct(
                id = BillingConstants.SKU_PRO_MONTHLY,
                name = "ShowTime Pro (Monthly)",
                description = "Monthly auto-renewing Pro subscription.",
                formattedPrice = "$1.99/mo",
                priceAmountMicros = 1990000L,
                priceCurrencyCode = "USD",
                productType = ProductType.SUBS,
                billingPeriod = "P1M",
                rawProductDetails = null
            )
        )
    }
}
