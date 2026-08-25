package com.ssverma.feature.account.ui.profile

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.core.storage.debug.DebugProOverride
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.shared.data.repository.BackupRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.TraktSyncRepository
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
    val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository,
    private val debugConfigManager: DebugConfigManager,
    private val libraryRepository: LibraryRepository
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

        // Observe Debug Configurations
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

        // Observe Trakt Auth State
        viewModelScope.launch {
            traktAuthManager.authState.collectLatest { traktState ->
                _uiState.update { it.copy(traktAuthState = traktState) }
            }
        }

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

        // Observe Backup Frequency
        viewModelScope.launch {
            backupRepository.backupFrequency.collectLatest { frequency ->
                _uiState.update { it.copy(backupFrequency = frequency) }
            }
        }

        // Observe Backup Over Wi-Fi Only
        viewModelScope.launch {
            backupRepository.backupOverWifiOnly.collectLatest { wifiOnly ->
                _uiState.update { it.copy(backupOverWifiOnly = wifiOnly) }
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
                    it.copy(
                        message = UiText.StaticText(
                            R.string.google_sign_in_success,
                            user.displayName
                        )
                    )
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

    fun onBackupFrequencySelected(frequency: BackupFrequency) {
        if (frequency.isAutomated && !_uiState.value.isProActive) {
            openPaywall()
        } else {
            viewModelScope.launch {
                backupRepository.setBackupFrequency(frequency)
            }
        }
    }

    fun onBackupOverWifiOnlyChanged(wifiOnly: Boolean) {
        viewModelScope.launch {
            backupRepository.setBackupOverWifiOnly(wifiOnly)
        }
    }

    fun openTraktConnect() {
        if (!_uiState.value.isProActive) {
            openPaywall()
        } else {
            _uiState.update { it.copy(isTraktConnectSheetVisible = true) }
        }
    }

    fun closeTraktConnect() {
        _uiState.update { it.copy(isTraktConnectSheetVisible = false) }
    }

    fun syncTraktNow() {
        val traktState = _uiState.value.traktAuthState
        if (traktState !is TraktAuthState.Connected) {
            openTraktConnect()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isTraktSyncing = true) }
            val result = traktSyncRepository.syncLibrary(traktState.accessToken)
            _uiState.update { it.copy(isTraktSyncing = false) }

            result.onSuccess { syncResult ->
                val totalSynced = syncResult.itemsImportedToWatchlist + syncResult.itemsImportedToHistory + syncResult.itemsExportedToTrakt
                _uiState.update {
                    it.copy(
                        message = UiText.StaticText(
                            R.string.trakt_sync_success,
                            totalSynced
                        )
                    )
                }
            }.onFailure { e ->
                Log.e("ProfileViewModel", "Trakt sync failed", e)
                _uiState.update { it.copy(message = UiText.StaticText(R.string.trakt_sync_failed)) }
            }
        }
    }

    fun disconnectTrakt() {
        viewModelScope.launch {
            traktAuthManager.disconnect()
        }
    }

    fun logout() {
        _uiState.update { it.copy(profileContent = ProfileContentState.Loading) }
        authManager.logout()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    // --- Developer / Debug Panel Methods ---

    fun openDeveloperPanel() {
        _uiState.update { it.copy(isDeveloperPanelVisible = true) }
    }

    fun dismissDeveloperPanel() {
        _uiState.update { it.copy(isDeveloperPanelVisible = false) }
    }

    fun setDebugProOverride(override: DebugProOverride) {
        debugConfigManager.setProOverride(override)
    }

    fun setDebugMockTraktEnabled(enabled: Boolean) {
        debugConfigManager.setMockTraktEnabled(enabled)
    }

    fun saveDebugCustomTraktClientId(clientId: String) {
        debugConfigManager.setCustomTraktClientId(clientId)
        _uiState.update { it.copy(message = UiText.DynamicText("Custom Client ID saved")) }
    }

    fun setDebugAdsDisabled(disabled: Boolean) {
        debugConfigManager.setAdsDisabled(disabled)
    }

    fun instantMockConnectTrakt() {
        traktAuthManager.instantMockConnect()
        _uiState.update { it.copy(message = UiText.DynamicText("Mock Trakt connected as @cinephile_dev")) }
    }

    fun seedSampleFavorites() {
        viewModelScope.launch {
            val samples = listOf(
                Triple(157336, "Interstellar", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"),
                Triple(438631, "Dune", "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg"),
                Triple(27205, "Inception", "https://image.tmdb.org/t/p/w500/ljsZTbVsrQSqZgWeep2P1QiDKuh.jpg"),
                Triple(93405, "Severance", "https://image.tmdb.org/t/p/w500/abfJnkhz4c24t1GqSgq2J61z4e2.jpg"),
                Triple(94997, "House of the Dragon", "https://image.tmdb.org/t/p/w500/7QMsOTMUswlwxJP0rTTZfmz2tX2.jpg")
            )
            samples.forEach { (id, title, poster) ->
                libraryRepository.toggleFavorite(
                    mediaId = id,
                    mediaType = if (id > 50000) MediaType.Tv else MediaType.Movie,
                    title = title,
                    posterImageUrl = poster,
                    backdropImageUrl = "",
                    voteAvg = 8.5f,
                    releaseDate = "2024-01-01"
                )
            }
            _uiState.update { it.copy(message = UiText.DynamicText("Seeded 5 favorites!")) }
        }
    }

    fun seedSampleWatchlist() {
        viewModelScope.launch {
            val samples = listOf(
                Triple(693134, "Dune: Part Two", "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg"),
                Triple(872585, "Oppenheimer", "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),
                Triple(136315, "The Bear", "https://image.tmdb.org/t/p/w500/rE4663pT2Gk62h1P9iQ8h5l7e7k.jpg"),
                Triple(76331, "Succession", "https://image.tmdb.org/t/p/w500/7TafgV4rYpZp3B2E9V5bY5q4N.jpg"),
                Triple(119051, "Wednesday", "https://image.tmdb.org/t/p/w500/9PFonQ921cwhEj6TaScOMqBh498.jpg")
            )
            samples.forEach { (id, title, poster) ->
                libraryRepository.toggleWatchlist(
                    mediaId = id,
                    mediaType = if (id > 50000) MediaType.Tv else MediaType.Movie,
                    title = title,
                    posterImageUrl = poster,
                    backdropImageUrl = "",
                    voteAvg = 8.5f,
                    releaseDate = "2024-01-01"
                )
            }
            _uiState.update { it.copy(message = UiText.DynamicText("Seeded 5 watchlist items!")) }
        }
    }

    fun seedSampleHistory() {
        viewModelScope.launch {
            val samples = listOf(
                Triple(550, "Fight Club", "https://image.tmdb.org/t/p/w500/bptfVGEQuv6vDTIMVCHjJ9Dz8PX.jpg"),
                Triple(680, "Pulp Fiction", "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"),
                Triple(1396, "Breaking Bad", "https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg"),
                Triple(60059, "Better Call Saul", "https://image.tmdb.org/t/p/w500/fC2HDm5t0kHsf793TmYRtkGIw73.jpg"),
                Triple(155, "The Dark Knight", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg")
            )
            samples.forEach { (id, title, poster) ->
                libraryRepository.logWatchHistory(
                    mediaId = id,
                    mediaType = if (id > 50000) MediaType.Tv else MediaType.Movie,
                    title = title,
                    posterImageUrl = poster,
                    voteAvg = 8.8f
                )
            }
            _uiState.update { it.copy(message = UiText.DynamicText("Seeded 5 watch history items!")) }
        }
    }

    fun clearLocalDatabase() {
        viewModelScope.launch {
            libraryRepository.clearAllLibrary()
            _uiState.update { it.copy(message = UiText.DynamicText("All local data wiped (Favorites, Watchlist, History, Episodes, Up Next)!")) }
        }
    }

    fun resetAllDebugOverrides() {
        debugConfigManager.resetAll()
        _uiState.update { it.copy(message = UiText.DynamicText("Debug overrides reset.")) }
    }
}