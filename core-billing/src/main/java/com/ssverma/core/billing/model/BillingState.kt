package com.ssverma.core.billing.model

sealed interface BillingState {
    data object Disconnected : BillingState
    data object Connecting : BillingState
    data object Connected : BillingState
    data class Error(val responseCode: Int, val message: String) : BillingState
}

sealed interface ProStatus {
    data class Active(
        val productId: String,
        val purchaseToken: String,
        val isLifetime: Boolean
    ) : ProStatus

    data object Inactive : ProStatus
    data object Loading : ProStatus
}

sealed interface PurchaseResult {
    data class Success(val purchaseToken: String, val productId: String) : PurchaseResult
    data object UserCancelled : PurchaseResult
    data class Error(val responseCode: Int, val message: String) : PurchaseResult
}
