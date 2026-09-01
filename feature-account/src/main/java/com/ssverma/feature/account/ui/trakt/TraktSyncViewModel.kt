package com.ssverma.feature.account.ui.trakt

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassType
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.shared.domain.repository.TraktSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraktSyncViewModel @Inject constructor(
    val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TraktSyncUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            traktAuthManager.authState.collectLatest { traktState ->
                _uiState.update { it.copy(traktAuthState = traktState) }
            }
        }

        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { it.copy(isProActive = isPro) }
            }
        }

        viewModelScope.launch {
            combine(
                billingRepository.isProActive,
                rewardManager.passStatus
            ) { isPro, passStatus ->
                isPro || passStatus.isTraktSyncUnlocked
            }.collectLatest { isUnlocked ->
                _uiState.update { it.copy(isTraktSyncUnlocked = isUnlocked) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(availableProducts = billingRepository.getAvailableProducts()) }
        }

        // Preload rewarded ad
        rewardedAdManager.loadAd()
    }

    fun onConnectTraktClicked() {
        if (_uiState.value.isTraktSyncUnlocked) {
            openTraktConnect()
        } else {
            _uiState.update { it.copy(isQuotaGateVisible = true) }
            rewardedAdManager.loadAd()
        }
    }

    fun dismissQuotaGate() {
        _uiState.update { it.copy(isQuotaGateVisible = false, isAdLoading = false) }
    }

    fun watchAdForTraktPass(activity: Activity) {
        _uiState.update { it.copy(isAdLoading = true) }
        rewardedAdManager.showRewardedAdIfReady(activity) {
            viewModelScope.launch {
                rewardManager.grantRewardPass(RewardPassType.TRAKT_SYNC)
                _uiState.update {
                    it.copy(
                        isQuotaGateVisible = false,
                        isAdLoading = false,
                        message = UiText.DynamicText("24-Hour Trakt Sync Pass Unlocked! 🎬")
                    )
                }
                openTraktConnect()
            }
        }
    }

    fun openTraktConnect() {
        _uiState.update { it.copy(isTraktConnectSheetVisible = true) }
    }

    fun closeTraktConnect() {
        _uiState.update { it.copy(isTraktConnectSheetVisible = false) }
    }

    fun openPaywall() {
        _uiState.update { it.copy(isPaywallVisible = true, isQuotaGateVisible = false) }
        viewModelScope.launch {
            _uiState.update { it.copy(availableProducts = billingRepository.getAvailableProducts()) }
        }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(isPaywallVisible = false) }
    }

    fun purchaseProduct(activity: Activity, product: BillingProduct) {
        viewModelScope.launch {
            billingRepository.purchaseProduct(activity, product)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoringPurchases = true) }
            val success = billingRepository.restorePurchases()
            _uiState.update {
                it.copy(
                    isRestoringPurchases = false,
                    message = if (success) {
                        UiText.StaticText(R.string.restore_success)
                    } else {
                        UiText.StaticText(R.string.restore_not_found)
                    }
                )
            }
        }
    }

    fun syncTraktNow() {
        val currentAuth = traktAuthManager.authState.value
        if (currentAuth !is TraktAuthState.Connected) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTraktSyncing = true) }
            val result = traktSyncRepository.syncLibrary(accessToken = currentAuth.accessToken)
            result.onSuccess { syncResult ->
                val total = syncResult.itemsImportedToWatchlist +
                        syncResult.itemsImportedToHistory +
                        syncResult.itemsExportedToTrakt
                _uiState.update {
                    it.copy(
                        isTraktSyncing = false,
                        message = UiText.StaticText(
                            R.string.trakt_sync_success,
                            total
                        )
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isTraktSyncing = false,
                        message = UiText.StaticText(R.string.trakt_sync_failed)
                    )
                }
            }
        }
    }

    fun disconnectTrakt() {
        viewModelScope.launch {
            traktAuthManager.disconnect()
            _uiState.update {
                it.copy(
                    message = UiText.DynamicText("Disconnected from Trakt.tv")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
