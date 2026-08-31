package com.ssverma.feature.account.ui.backup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupSyncViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val billingRepository: BillingRepository
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
            }
        }
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
        viewModelScope.launch {
            backupRepository.setBackupFrequency(frequency = frequency)
        }
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
