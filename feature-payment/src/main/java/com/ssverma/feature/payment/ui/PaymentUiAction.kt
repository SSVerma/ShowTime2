package com.ssverma.feature.payment.ui

import android.app.Activity
import com.ssverma.core.billing.model.BillingProduct

sealed interface PaymentUiAction {
    data class Purchase(val activity: Activity, val product: BillingProduct) : PaymentUiAction
    data object Restore : PaymentUiAction
    data object ManageSubscription : PaymentUiAction
}
