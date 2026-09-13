package com.ssverma.core.billing

object BillingConstants {
    // ShowTime Pro Subscription Products (4-Tier Recurring Model)
    const val SKU_PRO_YEARLY = "showtime_pro_yearly"
    const val SKU_PRO_SIX_MONTHS = "showtime_pro_six_months"
    const val SKU_PRO_THREE_MONTHS = "showtime_pro_three_months"
    const val SKU_PRO_MONTHLY = "showtime_pro_monthly"

    val SUBS_SKUS = listOf(
        SKU_PRO_YEARLY,
        SKU_PRO_SIX_MONTHS,
        SKU_PRO_THREE_MONTHS,
        SKU_PRO_MONTHLY
    )
}

