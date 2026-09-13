package com.ssverma.common.ui.paywall

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.common.ui.paywall.component.PaywallActiveIndicator
import com.ssverma.common.ui.paywall.component.PaywallDisabledCard
import com.ssverma.common.ui.paywall.component.PaywallFeaturesList
import com.ssverma.common.ui.paywall.component.PaywallHeader
import com.ssverma.common.ui.paywall.component.PaywallPlanCard
import com.ssverma.common.ui.paywall.component.PaywallPriceHelper
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallBottomSheet(
    products: List<BillingProduct>,
    isProActive: Boolean,
    isRestoring: Boolean,
    onPurchaseClick: (Activity, BillingProduct) -> Unit,
    onRestoreClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    isPurchasing: Boolean = false,
    errorMessage: String? = null,
    onManageSubscriptionClick: (() -> Unit)? = null,
    isPaywallRemoteEnabled: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    var selectedProductId by remember(products) {
        mutableStateOf(
            products.firstOrNull { it.id == BillingConstants.SKU_PRO_YEARLY }?.id
                ?: products.firstOrNull()?.id
                ?: BillingConstants.SKU_PRO_YEARLY
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showImmediateSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(bottom = MaterialTheme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PaywallHeader()

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

                PaywallFeaturesList()

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

                if (!isProActive) {
                    if (!isPaywallRemoteEnabled || products.isEmpty()) {
                        PaywallDisabledCard()

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        Button(
                            onClick = onDismissRequest,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_purchases_unavailable_cta),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            products.forEach { product ->
                                val isSelected = product.id == selectedProductId
                                val isYearly = product.id == BillingConstants.SKU_PRO_YEARLY
                                val savingsPercent = remember(product, products) {
                                    PaywallPriceHelper.calculateSavingsPercent(product, products)
                                }
                                val monthlyEquivalent = remember(product) {
                                    PaywallPriceHelper.calculateMonthlyEquivalentPrice(product)
                                }

                                PaywallPlanCard(
                                    product = product,
                                    isSelected = isSelected,
                                    isBestValue = isYearly,
                                    savingsPercent = savingsPercent,
                                    effectiveMonthlyPrice = monthlyEquivalent,
                                    onClick = { selectedProductId = product.id }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

                        Button(
                            onClick = {
                                val selectedProduct =
                                    products.firstOrNull { it.id == selectedProductId }
                                if (activity != null && selectedProduct != null) {
                                    onPurchaseClick(activity, selectedProduct)
                                }
                            },
                            enabled = !isPurchasing && !isRestoring,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (isPurchasing) {
                                ShowTimeLoadingIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = stringResource(R.string.upgrade_to_pro),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    PaywallActiveIndicator(
                        onManageSubscriptionClick = onManageSubscriptionClick ?: {
                            val uri =
                                Uri.parse("https://play.google.com/store/account/subscriptions")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                TextButton(
                    onClick = onRestoreClick,
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        ShowTimeLoadingIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    }
                    Text(
                        text = stringResource(R.string.restore_purchases),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            ShowTimeSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    }
}
