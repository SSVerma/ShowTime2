package com.ssverma.feature.payment.ui

import androidx.compose.runtime.Immutable
import com.ssverma.core.billing.model.BillingProduct

@Immutable
data class PaymentUiState(
    val products: List<BillingProduct> = emptyList(),
    val isProActive: Boolean = false,
    val isRestoring: Boolean = false,
    val isPurchasing: Boolean = false,
    val isPaywallRemoteEnabled: Boolean = true,
    val errorMessage: String? = null
)

sealed interface RestoreEvent {
    data object Success : RestoreEvent
    data object NotFound : RestoreEvent
}

sealed interface PurchaseUiEvent {
    data object Success : PurchaseUiEvent
    data class Error(val message: String? = null) : PurchaseUiEvent
}
