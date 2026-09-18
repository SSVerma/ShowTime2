package com.ssverma.common.ui.paywall

import com.google.common.truth.Truth.assertThat
import com.ssverma.common.ui.paywall.component.PaywallPriceHelper
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.ProductType
import org.junit.Test

class PaywallPriceHelperTest {

    private val monthly = BillingProduct(
        id = BillingConstants.SKU_PRO_MONTHLY,
        name = "Monthly",
        description = "",
        formattedPrice = "$1.99",
        priceAmountMicros = 1_990_000,
        priceCurrencyCode = "USD",
        productType = ProductType.SUBS,
        billingPeriod = "P1M"
    )

    private val threeMonths = BillingProduct(
        id = BillingConstants.SKU_PRO_THREE_MONTHS,
        name = "3 Months",
        description = "",
        formattedPrice = "$4.99",
        priceAmountMicros = 4_990_000,
        priceCurrencyCode = "USD",
        productType = ProductType.SUBS,
        billingPeriod = "P3M"
    )

    private val sixMonths = BillingProduct(
        id = BillingConstants.SKU_PRO_SIX_MONTHS,
        name = "6 Months",
        description = "",
        formattedPrice = "$7.99",
        priceAmountMicros = 7_990_000,
        priceCurrencyCode = "USD",
        productType = ProductType.SUBS,
        billingPeriod = "P6M"
    )

    private val yearly = BillingProduct(
        id = BillingConstants.SKU_PRO_YEARLY,
        name = "Yearly",
        description = "",
        formattedPrice = "$11.99",
        priceAmountMicros = 11_990_000,
        priceCurrencyCode = "USD",
        productType = ProductType.SUBS,
        billingPeriod = "P1Y"
    )

    private val allProducts = listOf(yearly, sixMonths, threeMonths, monthly)

    @Test
    fun `getPlanDurationMonths returns correct months for each SKU`() {
        assertThat(PaywallPriceHelper.getPlanDurationMonths(BillingConstants.SKU_PRO_YEARLY)).isEqualTo(
            12
        )
        assertThat(PaywallPriceHelper.getPlanDurationMonths(BillingConstants.SKU_PRO_SIX_MONTHS)).isEqualTo(
            6
        )
        assertThat(PaywallPriceHelper.getPlanDurationMonths(BillingConstants.SKU_PRO_THREE_MONTHS)).isEqualTo(
            3
        )
        assertThat(PaywallPriceHelper.getPlanDurationMonths(BillingConstants.SKU_PRO_MONTHLY)).isEqualTo(
            1
        )

        assertThat(PaywallPriceHelper.getPlanDurationMonths(yearly)).isEqualTo(12)
        assertThat(PaywallPriceHelper.getPlanDurationMonths(sixMonths)).isEqualTo(6)
        assertThat(PaywallPriceHelper.getPlanDurationMonths(threeMonths)).isEqualTo(3)
        assertThat(PaywallPriceHelper.getPlanDurationMonths(monthly)).isEqualTo(1)
    }

    @Test
    fun `calculateSavingsPercent calculates expected savings for multi-month tiers`() {
        val yearlySavings = PaywallPriceHelper.calculateSavingsPercent(yearly, allProducts)
        assertThat(yearlySavings).isEqualTo(49)

        val sixMonthsSavings = PaywallPriceHelper.calculateSavingsPercent(sixMonths, allProducts)
        assertThat(sixMonthsSavings).isEqualTo(33)

        val threeMonthsSavings =
            PaywallPriceHelper.calculateSavingsPercent(threeMonths, allProducts)
        assertThat(threeMonthsSavings).isEqualTo(16)

        val monthlySavings = PaywallPriceHelper.calculateSavingsPercent(monthly, allProducts)
        assertThat(monthlySavings).isNull()
    }

    @Test
    fun `calculateMonthlyEquivalentPrice returns formatted monthly price`() {
        val yearlyMonthly = PaywallPriceHelper.calculateMonthlyEquivalentPrice(yearly)
        assertThat(yearlyMonthly).contains("1.00")

        val sixMonthsMonthly = PaywallPriceHelper.calculateMonthlyEquivalentPrice(sixMonths)
        assertThat(sixMonthsMonthly).contains("1.33")

        val threeMonthsMonthly = PaywallPriceHelper.calculateMonthlyEquivalentPrice(threeMonths)
        assertThat(threeMonthsMonthly).contains("1.66")

        val monthlyResult = PaywallPriceHelper.calculateMonthlyEquivalentPrice(monthly)
        assertThat(monthlyResult).isNull()
    }

    @Test
    fun `calculateMonthlyEquivalentPrice formats INR prices correctly`() {
        val inrYearly = BillingProduct(
            id = BillingConstants.SKU_PRO_YEARLY,
            name = "Yearly",
            description = "",
            formattedPrice = "₹999",
            priceAmountMicros = 999_000_000,
            priceCurrencyCode = "INR",
            productType = ProductType.SUBS,
            billingPeriod = "P1Y"
        )
        val inrMonthly = PaywallPriceHelper.calculateMonthlyEquivalentPrice(inrYearly)
        assertThat(inrMonthly).contains("83.25")
        assertThat(inrMonthly).contains("₹")
    }

    @Test
    fun `getCleanPrice strips period suffixes cleanly`() {
        assertThat(PaywallPriceHelper.getCleanPrice(yearly)).isEqualTo("$11.99")
        val suffixedYearly = yearly.copy(formattedPrice = "$11.99/yr")
        assertThat(PaywallPriceHelper.getCleanPrice(suffixedYearly)).isEqualTo("$11.99")

        val inrSuffixed = yearly.copy(formattedPrice = "₹999/yr")
        assertThat(PaywallPriceHelper.getCleanPrice(inrSuffixed)).isEqualTo("₹999")
    }
}
