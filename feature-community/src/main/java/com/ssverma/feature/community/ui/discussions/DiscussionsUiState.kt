package com.ssverma.feature.community.ui.discussions

import androidx.compose.runtime.Immutable
import com.ssverma.common.ui.community.CommentUiModel
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ui.UiText

@Immutable
data class DiscussionsUiState(
    val isLoading: Boolean = true,
    val comments: List<CommentUiModel> = emptyList(),
    val isQuotaGateVisible: Boolean = false,
    val isAdLoading: Boolean = false,
    val isProPaymentEnabled: Boolean = true,
    val isPaywallVisible: Boolean = false,
    val isPurchasingProduct: Boolean = false,
    val isRestoringPurchases: Boolean = false,
    val paywallErrorMessage: UiText? = null,
    val availableProducts: List<BillingProduct> = emptyList(),
    val isProActive: Boolean = false
)
