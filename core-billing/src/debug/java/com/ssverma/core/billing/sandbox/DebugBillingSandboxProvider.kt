package com.ssverma.core.billing.sandbox

import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.ProductType
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugBillingSandboxProvider @Inject constructor() : BillingSandboxProvider {

    override fun isSandboxEnabled(): Boolean = true

    override fun getSandboxProducts(): List<BillingProduct> {
        val isIndiaLocale = isIndiaRegion()

        return if (isIndiaLocale) {
            listOf(
                BillingProduct(
                    id = BillingConstants.SKU_PRO_YEARLY,
                    name = "ShowTime Pro (Yearly)",
                    description = "Annual auto-renewing Pro subscription.",
                    formattedPrice = "₹999/yr",
                    priceAmountMicros = 999_000_000L,
                    priceCurrencyCode = "INR",
                    productType = ProductType.SUBS,
                    billingPeriod = "P1Y",
                    rawProductDetails = null
                ),
                BillingProduct(
                    id = BillingConstants.SKU_PRO_SIX_MONTHS,
                    name = "ShowTime Pro (6 Months)",
                    description = "6-month auto-renewing Pro subscription.",
                    formattedPrice = "₹599/6mo",
                    priceAmountMicros = 599_000_000L,
                    priceCurrencyCode = "INR",
                    productType = ProductType.SUBS,
                    billingPeriod = "P6M",
                    rawProductDetails = null
                ),
                BillingProduct(
                    id = BillingConstants.SKU_PRO_THREE_MONTHS,
                    name = "ShowTime Pro (3 Months)",
                    description = "3-month auto-renewing Pro subscription.",
                    formattedPrice = "₹349/3mo",
                    priceAmountMicros = 349_000_000L,
                    priceCurrencyCode = "INR",
                    productType = ProductType.SUBS,
                    billingPeriod = "P3M",
                    rawProductDetails = null
                ),
                BillingProduct(
                    id = BillingConstants.SKU_PRO_MONTHLY,
                    name = "ShowTime Pro (Monthly)",
                    description = "Monthly auto-renewing Pro subscription.",
                    formattedPrice = "₹149/mo",
                    priceAmountMicros = 149_000_000L,
                    priceCurrencyCode = "INR",
                    productType = ProductType.SUBS,
                    billingPeriod = "P1M",
                    rawProductDetails = null
                )
            )
        } else {
            listOf(
                BillingProduct(
                    id = BillingConstants.SKU_PRO_YEARLY,
                    name = "ShowTime Pro (Yearly)",
                    description = "Annual auto-renewing Pro subscription.",
                    formattedPrice = "$11.99/yr",
                    priceAmountMicros = 11_990_000L,
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
                    priceAmountMicros = 7_990_000L,
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
                    priceAmountMicros = 4_990_000L,
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
                    priceAmountMicros = 1_990_000L,
                    priceCurrencyCode = "USD",
                    productType = ProductType.SUBS,
                    billingPeriod = "P1M",
                    rawProductDetails = null
                )
            )
        }
    }

    private fun isIndiaRegion(): Boolean {
        return try {
            val defaultLocale = Locale.getDefault()
            val country = defaultLocale.country
            if (country.equals("IN", ignoreCase = true)) return true
            val currency = Currency.getInstance(defaultLocale)
            currency.currencyCode.equals("INR", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }
}
