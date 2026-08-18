package com.ssverma.feature.account.ui.profile

import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.AppTheme

data class ProfileScreenState(
    val profileContent: ProfileContentState = ProfileContentState.Loading,
    val isProActive: Boolean = false,
    val isPaywallVisible: Boolean = false,
    val isPaywallRemoteEnabled: Boolean = false,
    val availableProducts: List<BillingProduct> = emptyList(),
    val isRestoringPurchases: Boolean = false,
    val currentTheme: AppTheme = AppTheme.System,
    val message: String? = null
)

sealed interface ProfileContentState {
    data class Success(val profile: Profile) : ProfileContentState
    data object Loading : ProfileContentState
    data class Error(val failure: Failure.CoreFailure) : ProfileContentState
}