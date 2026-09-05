package com.ssverma.feature.account.ui.backup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.isGoogleSignInCancelled
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.shared.data.repository.BackupRepository
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
    private val rewardedAdManager: RewardedAdManager
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
        viewModelScope.launch {
            val result = backupRepository.backupNow()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.backup_success)
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.backup_failed)
                    )
                }
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = backupRepository.restoreBackup()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.restore_success_msg)
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(R.string.restore_failed)
                    )
                }
            }
        }
    }

    fun onBackupFrequencySelected(frequency: BackupFrequency) {
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
