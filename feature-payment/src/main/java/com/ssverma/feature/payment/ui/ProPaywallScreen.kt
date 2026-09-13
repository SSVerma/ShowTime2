package com.ssverma.feature.payment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.paywall.component.PaywallActiveIndicator
import com.ssverma.common.ui.paywall.component.PaywallDisabledCard
import com.ssverma.common.ui.paywall.component.PaywallFeaturesList
import com.ssverma.common.ui.paywall.component.PaywallHeader
import com.ssverma.common.ui.paywall.component.PaywallPlanCard
import com.ssverma.common.ui.paywall.component.PaywallPriceHelper
import com.ssverma.core.billing.BillingConstants
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.payment.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProPaywallScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }

    val restoreSuccessMsg = stringResource(R.string.restore_success)
    val restoreNotFoundMsg = stringResource(R.string.restore_not_found)
    val purchaseSuccessMsg = stringResource(R.string.purchase_success)
    val purchaseFailedMsg = stringResource(R.string.purchase_failed)

    LaunchedEffect(Unit) {
        viewModel.restoreEvents.collect { event ->
            val message = when (event) {
                RestoreEvent.Success -> restoreSuccessMsg
                RestoreEvent.NotFound -> restoreNotFoundMsg
            }
            snackbarHostState.showSnackbar(message = message)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.purchaseUiEvents.collect { event ->
            val message = when (event) {
                PurchaseUiEvent.Success -> purchaseSuccessMsg
                is PurchaseUiEvent.Error -> event.message ?: purchaseFailedMsg
            }
            snackbarHostState.showSnackbar(message = message)
        }
    }

    var selectedProductId by remember(uiState.products) {
        mutableStateOf(
            uiState.products.firstOrNull { it.id == BillingConstants.SKU_PRO_YEARLY }?.id
                ?: uiState.products.firstOrNull()?.id
                ?: BillingConstants.SKU_PRO_YEARLY
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.statusBarsPadding()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PaywallHeader()

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            PaywallFeaturesList()

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            if (!uiState.isProActive) {
                if (!uiState.isPaywallRemoteEnabled || uiState.products.isEmpty()) {
                    PaywallDisabledCard()

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                    Button(
                        onClick = onBackPressed,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
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
                        uiState.products.forEach { product ->
                            val isSelected = product.id == selectedProductId
                            val isYearly = product.id == BillingConstants.SKU_PRO_YEARLY
                            val savingsPercent = remember(product, uiState.products) {
                                PaywallPriceHelper.calculateSavingsPercent(
                                    product,
                                    uiState.products
                                )
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

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                    uiState.errorMessage?.let { error ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.small
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    Button(
                        onClick = {
                            val selectedProduct =
                                uiState.products.firstOrNull { it.id == selectedProductId }
                            if (activity != null && selectedProduct != null) {
                                viewModel.purchaseProduct(
                                    activity = activity,
                                    product = selectedProduct
                                )
                            }
                        },
                        enabled = !uiState.isPurchasing && !uiState.isRestoring,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (uiState.isPurchasing) {
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
                    onManageSubscriptionClick = {
                        val intent = viewModel.getManageSubscriptionsIntent()
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            TextButton(
                onClick = { viewModel.restorePurchases() },
                enabled = !uiState.isRestoring
            ) {
                if (uiState.isRestoring) {
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
    }
}
