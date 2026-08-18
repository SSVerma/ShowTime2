package com.ssverma.core.billing

object BillingConstants {
    // In-App One-Time Lifetime Product
    const val SKU_PRO_LIFETIME = "showtime_pro_lifetime"

    // Subscription Products
    const val SKU_PRO_ANNUAL = "showtime_pro_annual"
    const val SKU_PRO_MONTHLY = "showtime_pro_monthly"

    val INAPP_SKUS = listOf(SKU_PRO_LIFETIME)
    val SUBS_SKUS = listOf(SKU_PRO_ANNUAL, SKU_PRO_MONTHLY)
}
