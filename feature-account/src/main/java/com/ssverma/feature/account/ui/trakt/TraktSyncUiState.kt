package com.ssverma.feature.account.ui.trakt

import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ui.UiText
import com.ssverma.feature.auth.domain.model.TraktAuthState

data class TraktSyncUiState(
    val traktAuthState: TraktAuthState = TraktAuthState.Disconnected,
    val isTraktSyncing: Boolean = false,
    val isProActive: Boolean = false,
    val isTraktSyncUnlocked: Boolean = false,
    val isTraktConnectSheetVisible: Boolean = false,
    val isQuotaGateVisible: Boolean = false,
    val isPaywallVisible: Boolean = false,
    val isAdLoading: Boolean = false,
    val availableProducts: List<BillingProduct> = emptyList(),
    val isRestoringPurchases: Boolean = false,
    val message: UiText? = null
)
