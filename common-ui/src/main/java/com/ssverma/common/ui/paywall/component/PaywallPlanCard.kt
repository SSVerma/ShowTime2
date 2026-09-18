package com.ssverma.common.ui.paywall.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ui.theme.spacing
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object PaywallPriceHelper {
    private const val MICROS_PER_UNIT = 1_000_000.0

    fun getPlanDurationMonths(product: BillingProduct): Int {
        product.billingPeriod?.let { period ->
            when (period.uppercase()) {
                "P1Y", "P12M" -> return 12
                "P6M" -> return 6
                "P3M" -> return 3
                "P1M" -> return 1
            }
        }
        return getPlanDurationMonths(product.id)
    }

    fun getPlanDurationMonths(productId: String): Int {
        return when (productId) {
            BillingConstants.SKU_PRO_YEARLY -> 12
            BillingConstants.SKU_PRO_SIX_MONTHS -> 6
            BillingConstants.SKU_PRO_THREE_MONTHS -> 3
            BillingConstants.SKU_PRO_MONTHLY -> 1
            else -> 1
        }
    }

    fun calculateAnnualSavingsPercent(products: List<BillingProduct>): Int? {
        val yearly = products.firstOrNull {
            it.id == BillingConstants.SKU_PRO_YEARLY
        } ?: return null
        return calculateSavingsPercent(yearly, products)
    }

    fun calculateSavingsPercent(product: BillingProduct, allProducts: List<BillingProduct>): Int? {
        val durationMonths = getPlanDurationMonths(product)
        if (durationMonths <= 1) return null

        val monthly =
            allProducts.firstOrNull { it.id == BillingConstants.SKU_PRO_MONTHLY } ?: return null
        if (product.priceAmountMicros <= 0 || monthly.priceAmountMicros <= 0) return null

        val fullPriceFromMonthly = monthly.priceAmountMicros * durationMonths.toDouble()
        val savings =
            ((fullPriceFromMonthly - product.priceAmountMicros) / fullPriceFromMonthly) * 100.0
        return if (savings > 0) savings.toInt() else null
    }

    fun calculateMonthlyEquivalentPrice(product: BillingProduct): String? {
        val durationMonths = getPlanDurationMonths(product)
        if (durationMonths <= 1 || product.priceAmountMicros <= 0) return null
        return try {
            val monthlyAmount =
                (product.priceAmountMicros / durationMonths.toDouble()) / MICROS_PER_UNIT
            val isWhole = (monthlyAmount % 1.0) == 0.0
            val fractionDigits = if (isWhole) 0 else 2

            if (product.priceCurrencyCode.equals("INR", ignoreCase = true)) {
                val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                    currency = Currency.getInstance("INR")
                    maximumFractionDigits = fractionDigits
                    minimumFractionDigits = fractionDigits
                }
                inrFormat.format(monthlyAmount)
            } else {
                val format = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                    currency = Currency.getInstance(product.priceCurrencyCode)
                    maximumFractionDigits = fractionDigits
                    minimumFractionDigits = fractionDigits
                }
                format.format(monthlyAmount)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getCleanPrice(product: BillingProduct): String {
        return product.formattedPrice
            .replace("/yr", "", ignoreCase = true)
            .replace("/6mo", "", ignoreCase = true)
            .replace("/3mo", "", ignoreCase = true)
            .replace("/mo", "", ignoreCase = true)
            .trim()
    }
}

@Composable
fun PaywallPlanCard(
    product: BillingProduct,
    isSelected: Boolean,
    isBestValue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    savingsPercent: Int? = null,
    effectiveMonthlyPrice: String? = null
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "planBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "planContainerColor"
    )

    OutlinedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.medium
                )
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = if (!isSelected) BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.outline
                ) else null,
                modifier = Modifier.size(20.dp)
            ) {
                if (isSelected) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.plan_selected),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (product.id) {
                            BillingConstants.SKU_PRO_YEARLY -> stringResource(R.string.plan_yearly)
                            BillingConstants.SKU_PRO_SIX_MONTHS -> stringResource(R.string.plan_six_months)
                            BillingConstants.SKU_PRO_THREE_MONTHS -> stringResource(R.string.plan_three_months)
                            BillingConstants.SKU_PRO_MONTHLY -> stringResource(R.string.plan_monthly)
                            else -> product.name
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val badgeText = when {
                        isBestValue && savingsPercent != null && savingsPercent > 0 -> {
                            stringResource(R.string.best_value_with_savings, savingsPercent)
                        }

                        isBestValue -> {
                            stringResource(R.string.best_value)
                        }

                        savingsPercent != null && savingsPercent > 0 -> {
                            stringResource(R.string.save_percent, savingsPercent)
                        }

                        else -> null
                    }

                    if (badgeText != null) {
                        val (badgeContainerColor, contentColor) = if (isBestValue) {
                            MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        PlanBadge(
                            text = badgeText,
                            containerColor = badgeContainerColor,
                            contentColor = contentColor
                        )
                    }
                }

                if (effectiveMonthlyPrice != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.plan_effective_monthly,
                            effectiveMonthlyPrice
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

            Text(
                text = PaywallPriceHelper.getCleanPrice(product),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PlanBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = MaterialTheme.spacing.small, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
    }
}
