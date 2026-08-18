package com.ssverma.feature.account.ui.profile

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.data.repository.BackupRepository
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.core.ui.UiText
import android.util.Log
import com.ssverma.feature.account.R
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager,
    private val billingRepository: BillingRepository,
    private val backupRepository: BackupRepository,
    private val appConfigRepository: AppConfigRepository,
    private val appConfigProvider: AppConfigProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        // Read remote config flag for paywall
        val isPaywallEnabledRemotely = appConfigProvider.getBoolean(
            key = "show_pro_paywall_enabled",
            defaultValue = true // Enabled in dev by default
        )
        _uiState.update { it.copy(isPaywallRemoteEnabled = isPaywallEnabledRemotely) }

        // Observe Pro Subscription Status
        viewModelScope.launch {
            billingRepository.isProActive.collectLatest { isPro ->
                _uiState.update { it.copy(isProActive = isPro) }
            }
        }

        // Observe Google User
        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { user ->
                _uiState.update { it.copy(googleUser = user) }
            }
        }

        // Observe Backup Status
        viewModelScope.launch {
            backupRepository.backupStatus.collectLatest { status ->
                _uiState.update { it.copy(backupStatus = status) }
            }
        }

        // Observe Last Backup Metadata
        viewModelScope.launch {
            backupRepository.lastBackupMetadata.collectLatest { metadata ->
                _uiState.update { it.copy(lastBackupMetadata = metadata) }
            }
        }

        // Observe Theme
        viewModelScope.launch {
            appConfigRepository.appTheme.collectLatest { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }

        // Load Billing Products
        viewModelScope.launch {
            val products = billingRepository.getAvailableProducts()
            _uiState.update { it.copy(availableProducts = products) }
        }

        // Observe Auth & fetch profile
        viewModelScope.launch {
            authManager.authFlow.collectLatest { authState ->
                if (authState is AuthState.Authorized.WithSession) {
                    fetchProfile()
                } else {
                    accountRepository.removeUserAccount()
                    _uiState.update {
                        it.copy(
                            profileContent = ProfileContentState.Success(
                                profile = com.ssverma.feature.account.domain.model.Profile(
                                    id = 0,
                                    userName = "guest",
                                    displayName = "Guest User",
                                    imageUrl = ""
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun fetchProfile() {
        _uiState.update { it.copy(profileContent = ProfileContentState.Loading) }

        viewModelScope.launch {
            val profileResult = accountRepository.fetchProfile(
                sessionId = authManager.sessionIdOrNull().orEmpty()
            )
            val newContentState = when (profileResult) {
                is Result.Error -> ProfileContentState.Success(
                    profile = com.ssverma.feature.account.domain.model.Profile(
                        id = 0,
                        userName = "guest",
                        displayName = "Guest User",
                        imageUrl = ""
                    )
                )
                is Result.Success -> ProfileContentState.Success(profile = profileResult.data)
            }
            _uiState.update { it.copy(profileContent = newContentState) }
        }
    }

    fun openPaywall() {
        _uiState.update { it.copy(isPaywallVisible = true) }
        viewModelScope.launch {
            val products = billingRepository.getAvailableProducts()
            _uiState.update { it.copy(availableProducts = products) }
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
        _uiState.update { it.copy(isRestoringPurchases = true) }
        viewModelScope.launch {
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

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            appConfigRepository.updateAppTheme(theme)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = backupRepository.signInWithGoogle(activity)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(message = UiText.StaticText(R.string.google_sign_in_success, user.displayName))
                }
            }.onFailure { e ->
                Log.e("ProfileViewModel", "Google Sign-In failed", e)
                val message = if (e.javaClass.simpleName.contains("Cancel", ignoreCase = true)) {
                    UiText.StaticText(R.string.google_sign_in_cancelled)
                } else {
                    UiText.StaticText(R.string.google_sign_in_failed)
                }
                _uiState.update { it.copy(message = message) }
            }
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            backupRepository.signOutGoogle()
            _uiState.update { it.copy(message = UiText.StaticText(R.string.google_signed_out)) }
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            val result = backupRepository.backupNow()
            result.onSuccess {
                _uiState.update { it.copy(message = UiText.StaticText(R.string.backup_success)) }
            }.onFailure { e ->
                Log.e("ProfileViewModel", "Backup failed", e)
                _uiState.update { it.copy(message = UiText.StaticText(R.string.backup_failed)) }
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = backupRepository.restoreBackup()
            result.onSuccess {
                _uiState.update { it.copy(message = UiText.StaticText(R.string.restore_success_msg)) }
            }.onFailure { e ->
                Log.e("ProfileViewModel", "Restore failed", e)
                _uiState.update { it.copy(message = UiText.StaticText(R.string.restore_failed)) }
            }
        }
    }

    fun logout() {
        _uiState.update { it.copy(profileContent = ProfileContentState.Loading) }
        authManager.logout()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}