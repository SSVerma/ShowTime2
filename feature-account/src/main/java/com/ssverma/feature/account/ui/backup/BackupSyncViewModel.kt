package com.ssverma.feature.account.ui.backup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.isGoogleSignInCancelled
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.AccountAnalyticsScreenName
import com.ssverma.feature.account.R
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.analytics.backup.BackupAnalyticsEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSyncViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val billingRepository: BillingRepository,
    private val rewardManager: RewardManager,
    private val rewardedAdManager: RewardedAdManager,
    private val analytics: Analytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupSyncUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            backupRepository.fetchRemoteBackupMetadata()
        }

        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { user ->
                _uiState.update { it.copy(googleUser = user) }
                if (user != null) {
                    backupRepository.fetchRemoteBackupMetadata()
                }
            }
        }

        viewModelScope.launch {
            backupRepository.backupStatus.collectLatest { status ->
                _uiState.update { it.copy(backupStatus = status) }
            }
        }

        viewModelScope.launch {
            backupRepository.lastBackupMetadata.collectLatest { metadata ->
                _uiState.update { it.copy(lastBackupMetadata = metadata) }
            }
        }

        viewModelScope.launch {
            backupRepository.backupFrequency.collectLatest { frequency ->
                _uiState.update { it.copy(backupFrequency = frequency) }
            }
        }

        viewModelScope.launch {
            backupRepository.backupOverWifiOnly.collectLatest { wifiOnly ->
                _uiState.update { it.copy(backupOverWifiOnly = wifiOnly) }
            }
        }

        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { it.copy(isProActive = isPro) }
                if (!isPro && _uiState.value.backupFrequency.isAutomated) {
                    backupRepository.setBackupFrequency(BackupFrequency.OFF)
                }
            }
        }

        rewardedAdManager.loadAd()
        refreshLocalItemCount()
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true) }
            val result = backupRepository.signInWithGoogle(activity = activity)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isSigningIn = false,
                        message = UiText.DynamicText(
                            "Signed in as ${user.displayName.ifBlank { user.email }}"
                        )
                    )
                }
                backupRepository.fetchRemoteBackupMetadata()
                refreshLocalItemCount()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSigningIn = false,
                        message = if (error.isGoogleSignInCancelled()) {
                            null
                        } else {
                            UiText.StaticText(R.string.google_sign_in_failed)
                        }
                    )
                }
            }
        }
    }

    private fun refreshLocalItemCount() {
        viewModelScope.launch {
            val count = backupRepository.getLocalItemCount()
            _uiState.update { it.copy(localItemCount = count) }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            backupRepository.signOutGoogle()
            _uiState.update {
                it.copy(
                    isSigningOut = false,
                    message = UiText.StaticText(R.string.google_signed_out)
                )
            }
        }
    }

    fun onAttemptBackupNow() {
        viewModelScope.launch {
            val isPro = billingRepository.isProActive.first()
            if (isPro) {
                backupNow()
            } else {
                _uiState.update { it.copy(isManualBackupGateVisible = true) }
                rewardedAdManager.loadAd()
            }
        }
    }

    fun watchAdForManualBackup(activity: Activity) {
        _uiState.update { it.copy(isAdLoading = true) }
        rewardedAdManager.showRewardedAdIfReady(activity) {
            _uiState.update { it.copy(isManualBackupGateVisible = false, isAdLoading = false) }
            backupNow()
        }
    }

    fun dismissManualBackupGate() {
        _uiState.update { it.copy(isManualBackupGateVisible = false, isAdLoading = false) }
    }

    fun backupNow() {
        analytics.logEvent(
            BackupAnalyticsEvent.BackupStarted(
                isAutomated = false,
                sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
            )
        )
        viewModelScope.launch {
            val result = backupRepository.backupNow()
            result.onSuccess {
                analytics.logEvent(
                    BackupAnalyticsEvent.BackupCompleted(
                        success = true,
                        isAutomated = false,
                        itemCount = _uiState.value.localItemCount,
                        sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
                    )
                )
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.backup_success)
                    )
                }
                refreshLocalItemCount()
            }.onFailure {
                analytics.logEvent(
                    BackupAnalyticsEvent.BackupCompleted(
                        success = false,
                        isAutomated = false,
                        sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
                    )
                )
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.backup_failed)
                    )
                }
            }
        }
    }

    fun restoreBackup() {
        analytics.logEvent(
            BackupAnalyticsEvent.RestoreStarted(
                sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
            )
        )
        viewModelScope.launch {
            val result = backupRepository.restoreBackup()
            result.onSuccess {
                analytics.logEvent(
                    BackupAnalyticsEvent.RestoreCompleted(
                        success = true,
                        sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
                    )
                )
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.restore_success_msg)
                    )
                }
                refreshLocalItemCount()
            }.onFailure {
                analytics.logEvent(
                    BackupAnalyticsEvent.RestoreCompleted(
                        success = false,
                        sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
                    )
                )
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.restore_failed)
                    )
                }
            }
        }
    }

    fun onBackupFrequencySelected(frequency: BackupFrequency) {
        analytics.logEvent(
            BackupAnalyticsEvent.FrequencyChanged(
                frequency = frequency.name,
                sourceScreen = AccountAnalyticsScreenName.BACKUP_SYNC
            )
        )
        if (frequency == BackupFrequency.OFF) {
            viewModelScope.launch {
                backupRepository.setBackupFrequency(BackupFrequency.OFF)
            }
            return
        }

        viewModelScope.launch {
            val isPro = billingRepository.isProActive.first()
            if (isPro) {
                backupRepository.setBackupFrequency(frequency)
            } else {
                _uiState.update { it.copy(isAutoBackupPaywallVisible = true) }
            }
        }
    }

    fun dismissAutoBackupPaywall() {
        _uiState.update { it.copy(isAutoBackupPaywallVisible = false) }
    }

    fun onBackupOverWifiOnlyChanged(enabled: Boolean) {
        viewModelScope.launch {
            backupRepository.setBackupOverWifiOnly(wifiOnly = enabled)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
