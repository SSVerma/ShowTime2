package com.ssverma.feature.account.ui.profile

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.core.storage.debug.DebugProOverride
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.account.domain.seeder.DatabaseSeeder
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.shared.data.repository.BackupRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ConfigurationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager,
    private val billingRepository: BillingRepository,
    private val backupRepository: BackupRepository,
    private val appConfigRepository: AppConfigRepository,
    private val configurationRepository: ConfigurationRepository,
    private val appConfigProvider: AppConfigProvider,
    private val traktAuthManager: TraktAuthManager,
    private val debugConfigManager: DebugConfigManager,
    private val optionalDatabaseSeeder: Optional<DatabaseSeeder>
) : ViewModel() {

    private val databaseSeeder: DatabaseSeeder
        get() = if (optionalDatabaseSeeder.isPresent) optionalDatabaseSeeder.get() else DatabaseSeeder.NoOp

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        val isPaywallEnabledRemotely = appConfigProvider.getBoolean(
            key = "show_pro_paywall_enabled",
            defaultValue = true
        )
        _uiState.update { it.copy(isPaywallRemoteEnabled = isPaywallEnabledRemotely) }

        viewModelScope.launch {
            debugConfigManager.proOverride.collectLatest { override ->
                _uiState.update { it.copy(proOverride = override) }
            }
        }
        viewModelScope.launch {
            debugConfigManager.isMockTraktEnabled.collectLatest { isMock ->
                _uiState.update { it.copy(isMockTraktEnabled = isMock) }
            }
        }
        viewModelScope.launch {
            debugConfigManager.customTraktClientId.collectLatest { clientId ->
                _uiState.update { it.copy(customTraktClientId = clientId) }
            }
        }
        viewModelScope.launch {
            debugConfigManager.isAdsDisabled.collectLatest { disabled ->
                _uiState.update { it.copy(isAdsDisabled = disabled) }
            }
        }

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
            backupRepository.googleUser.collectLatest { user ->
                _uiState.update { state ->
                    val updatedProfile = if (state.profileContent is ProfileContentState.Success) {
                        if (state.profileContent.profile.id == 0 && user != null) {
                            state.profileContent.profile.copy(
                                displayName = user.displayName,
                                imageUrl = user.photoUrl.orEmpty()
                            )
                        } else {
                            state.profileContent.profile
                        }
                    } else null
                    state.copy(
                        googleUser = user,
                        profileContent = if (updatedProfile != null)
                            ProfileContentState.Success(updatedProfile)
                        else state.profileContent
                    )
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
            appConfigRepository.appTheme.collectLatest { theme ->
                _uiState.update { it.copy(currentTheme = theme) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.isDynamicColorEnabled.collectLatest { dynamicColor ->
                _uiState.update { it.copy(isDynamicColorEnabled = dynamicColor) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.watchProviderRegion.collectLatest { region ->
                _uiState.update { it.copy(watchProviderRegion = region) }
            }
        }

        viewModelScope.launch {
            appConfigRepository.contentLanguage.collectLatest { language ->
                _uiState.update { it.copy(contentLanguage = language) }
            }
        }

        viewModelScope.launch {
            when (val result = configurationRepository.fetchCountries()) {
                is Result.Success -> {
                    _uiState.update { it.copy(availableRegions = result.data) }
                }

                else -> Unit
            }
        }

        viewModelScope.launch {
            when (val result = configurationRepository.fetchLanguages()) {
                is Result.Success -> {
                    _uiState.update { it.copy(availableLanguages = result.data) }
                }

                else -> Unit
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(availableProducts = billingRepository.getAvailableProducts()) }
        }

        viewModelScope.launch {
            authManager.authFlow.collectLatest { authState ->
                if (authState is AuthState.Authorized.WithSession) {
                    fetchProfile()
                } else {
                    accountRepository.removeUserAccount()
                    _uiState.update {
                        it.copy(
                            profileContent = ProfileContentState.Success(
                                profile = Profile(
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
        val user = backupRepository.googleUser.value
        val initialProfile = Profile(
            id = 0,
            userName = user?.displayName ?: "guest",
            displayName = user?.displayName ?: "Guest User",
            imageUrl = user?.photoUrl.orEmpty()
        )

        _uiState.update { state ->
            if (state.profileContent is ProfileContentState.Success) {
                state
            } else {
                state.copy(profileContent = ProfileContentState.Success(initialProfile))
            }
        }

        viewModelScope.launch {
            val sessionId = authManager.sessionIdOrNull()
            if (!sessionId.isNullOrBlank()) {
                val profileResult = accountRepository.fetchProfile(sessionId = sessionId)
                if (profileResult is Result.Success) {
                    _uiState.update { it.copy(profileContent = ProfileContentState.Success(profile = profileResult.data)) }
                }
            }
        }
    }

    fun openPaywall() {
        _uiState.update { it.copy(isPaywallVisible = true) }
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

    fun openThemeSheet() {
        _uiState.update { it.copy(isThemeSheetVisible = true) }
    }

    fun closeThemeSheet() {
        _uiState.update { it.copy(isThemeSheetVisible = false) }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            appConfigRepository.updateAppTheme(theme = theme)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            appConfigRepository.updateDynamicColor(enabled = enabled)
        }
    }

    fun openLocalizationSheet() {
        _uiState.update { it.copy(isLocalizationSheetVisible = true) }
    }

    fun closeLocalizationSheet() {
        _uiState.update { it.copy(isLocalizationSheetVisible = false) }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true) }
            val result = backupRepository.signInWithGoogle(activity)
            result.onSuccess { user ->
                _uiState.update { state ->
                    state.copy(
                        isSigningIn = false,
                        message = UiText.DynamicText("Signed in as ${user.displayName}")
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        isSigningIn = false,
                        message = UiText.StaticText(R.string.google_sign_in_failed)
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

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
            accountRepository.removeUserAccount()
            backupRepository.signOutGoogle()
            fetchProfile()
        }
    }

    fun openDeveloperPanel() {
        _uiState.update { it.copy(isDeveloperPanelVisible = true) }
    }

    fun dismissDeveloperPanel() {
        _uiState.update { it.copy(isDeveloperPanelVisible = false) }
    }

    fun setDebugProOverride(override: DebugProOverride) {
        viewModelScope.launch {
            debugConfigManager.setProOverride(override = override)
        }
    }

    fun setDebugMockTraktEnabled(enabled: Boolean) {
        viewModelScope.launch {
            debugConfigManager.setMockTraktEnabled(enabled = enabled)
        }
    }

    fun saveDebugCustomTraktClientId(clientId: String) {
        viewModelScope.launch {
            debugConfigManager.setCustomTraktClientId(clientId = clientId)
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_key_saved_msg)
                )
            }
        }
    }

    fun setDebugAdsDisabled(disabled: Boolean) {
        viewModelScope.launch {
            debugConfigManager.setAdsDisabled(disabled = disabled)
        }
    }

    fun instantMockConnectTrakt() {
        viewModelScope.launch {
            traktAuthManager.instantMockConnect()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_mock_connected_msg)
                )
            }
        }
    }

    fun disconnectTrakt() {
        viewModelScope.launch {
            traktAuthManager.disconnect()
        }
    }

    fun populateDemoFavorites() {
        viewModelScope.launch {
            databaseSeeder.seedFavorites()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_seeded_favorites_msg)
                )
            }
        }
    }

    fun populateDemoWatchlist() {
        viewModelScope.launch {
            databaseSeeder.seedWatchlist()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_seeded_watchlist_msg)
                )
            }
        }
    }

    fun populateDemoHistory() {
        viewModelScope.launch {
            databaseSeeder.seedHistory()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_seeded_history_msg)
                )
            }
        }
    }

    fun clearLocalDatabase() {
        viewModelScope.launch {
            databaseSeeder.clearDatabase()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_cleared_db_msg)
                )
            }
        }
    }

    fun resetCinemaGame() {
        viewModelScope.launch {
            databaseSeeder.resetCinemaGame()
            _uiState.update {
                it.copy(
                    message = UiText.DynamicText("Daily Cinema Challenge state reset!")
                )
            }
        }
    }

    fun resetAllDebugOverrides() {
        viewModelScope.launch {
            debugConfigManager.resetAll()
            _uiState.update {
                it.copy(
                    message = UiText.StaticText(R.string.dev_overrides_reset_msg)
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
