package com.ssverma.feature.account.ui.profile

import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.storage.debug.DebugProOverride
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion

data class ProfileScreenState(
    val profileContent: ProfileContentState = ProfileContentState.Loading,
    val isProActive: Boolean = false,
    val isPaywallVisible: Boolean = false,
    val isPaywallRemoteEnabled: Boolean = false,
    val availableProducts: List<BillingProduct> = emptyList(),
    val isRestoringPurchases: Boolean = false,
    val currentTheme: AppTheme = AppTheme.System,
    val isDynamicColorEnabled: Boolean = true,
    val isThemeSheetVisible: Boolean = false,
    val watchProviderRegion: String = "US",
    val availableRegions: List<WatchProviderRegion> = emptyList(),
    val contentLanguage: String = "en",
    val availableLanguages: List<Language> = emptyList(),
    val isLocalizationSheetVisible: Boolean = false,
    val googleUser: GoogleUser? = null,
    val isSigningIn: Boolean = false,
    val isSigningOut: Boolean = false,
    val backupStatus: BackupStatus = BackupStatus.Idle,
    val lastBackupMetadata: BackupMetadata? = null,
    val backupFrequency: BackupFrequency = BackupFrequency.OFF,
    val traktAuthState: TraktAuthState = TraktAuthState.Disconnected,
    val isDeveloperPanelVisible: Boolean = false,
    val proOverride: DebugProOverride = DebugProOverride.AUTO,
    val isMockTraktEnabled: Boolean = false,
    val customTraktClientId: String = "",
    val isAdsDisabled: Boolean = false,
    val message: UiText? = null
)

sealed interface ProfileContentState {
    data class Success(val profile: Profile) : ProfileContentState
    data object Loading : ProfileContentState
    data class Error(val failure: Failure.CoreFailure) : ProfileContentState
}