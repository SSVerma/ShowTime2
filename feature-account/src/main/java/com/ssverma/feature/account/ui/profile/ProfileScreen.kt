package com.ssverma.feature.account.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.common.ui.paywall.ProPaywallBottomSheet
import com.ssverma.common.ui.subscription.StreamingSubscriptionsBottomSheet
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.account.BuildConfig
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.ui.debug.DeveloperPanelBottomSheet
import com.ssverma.feature.account.ui.profile.component.GoogleSignInPromptCard
import com.ssverma.feature.account.ui.profile.component.PreferredGenresBottomSheet
import com.ssverma.feature.account.ui.profile.component.ProActiveBanner
import com.ssverma.feature.account.ui.profile.component.ProUpgradeBanner
import com.ssverma.feature.account.ui.profile.component.ProfileHeader
import com.ssverma.feature.account.ui.profile.component.SettingsNavGroup
import com.ssverma.feature.account.ui.profile.component.SignOutConfirmDialog
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.model.auth.TraktAuthState
import com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showImmediateSnackbar(
                message = msg.asString(context),
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Screen(
        title = stringResource(R.string.profile),
        onBackPressed = onBackPressed,
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    ) { innerPadding ->
        when (val content = uiState.profileContent) {
            is ProfileContentState.Success -> {
                ProfileContent(
                    profile = content.profile,
                    isProActive = uiState.isProActive,
                    isPaywallRemoteEnabled = uiState.isPaywallRemoteEnabled,
                    currentTheme = uiState.currentTheme,
                    watchProviderRegion = uiState.watchProviderRegion,
                    availableRegions = uiState.availableRegions,
                    contentLanguage = uiState.contentLanguage,
                    availableLanguages = uiState.availableLanguages,
                    userStreamingSubscriptions = uiState.userStreamingSubscriptions,
                    userSeededGenres = uiState.userSeededGenres,
                    isReleaseRadarEnabled = uiState.isReleaseRadarEnabled,
                    isReleaseRadarRemoteEnabled = uiState.isReleaseRadarRemoteEnabled,
                    onReleaseRadarToggled = { viewModel.updateReleaseRadarEnabled(it) },
                    googleUser = uiState.googleUser,
                    backupStatus = uiState.backupStatus,
                    lastBackupMetadata = uiState.lastBackupMetadata,
                    guestPseudonym = uiState.guestPseudonym,
                    isSigningIn = uiState.isSigningIn,
                    isSigningOut = uiState.isSigningOut,
                    traktAuthState = uiState.traktAuthState,
                    isMockTraktEnabled = uiState.isMockTraktEnabled,
                    onUpgradeClick = { viewModel.openPaywall() },
                    onOpenBackup = onOpenBackup,
                    onOpenTheme = { viewModel.openThemeSheet() },
                    onOpenLocalization = { viewModel.openLocalizationSheet() },
                    onOpenStreamingSubscriptions = { viewModel.openStreamingSubscriptionsSheet() },
                    onOpenPreferredGenres = { viewModel.openPreferredGenresSheet() },
                    onOpenAbout = onOpenAbout,
                    onLogoutClick = { showSignOutConfirmDialog = true },
                    onGoogleSignInClick = { activity?.let { viewModel.signInWithGoogle(it) } },
                    onOpenDeveloperPanelClick = { viewModel.openDeveloperPanel() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is ProfileContentState.Error -> {
                DefaultCoreErrorIndicator(
                    failure = content.failure,
                    onRetry = { viewModel.fetchProfile() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            ProfileContentState.Loading -> {
                ScreenLoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
        }

        // Sign Out Confirmation Dialog
        if (showSignOutConfirmDialog) {
            SignOutConfirmDialog(
                onConfirm = {
                    showSignOutConfirmDialog = false
                    viewModel.signOutGoogle()
                },
                onDismissRequest = { showSignOutConfirmDialog = false }
            )
        }

        // Theme Selection Bottom Sheet
        if (uiState.isThemeSheetVisible) {
            ThemeSelectionBottomSheet(
                currentTheme = uiState.currentTheme,
                isDynamicColorEnabled = uiState.isDynamicColorEnabled,
                isProActive = uiState.isProActive,
                onThemeSelected = { viewModel.updateTheme(it) },
                onDynamicColorToggled = { viewModel.updateDynamicColor(it) },
                onUpgradeToPro = {
                    viewModel.closeThemeSheet()
                    viewModel.openPaywall()
                },
                onDismissRequest = { viewModel.closeThemeSheet() }
            )
        }

        // Localization (Region & Language) Bottom Sheet
        if (uiState.isLocalizationSheetVisible) {
            LocalizationSettingsBottomSheet(
                onDismissRequest = { viewModel.closeLocalizationSheet() }
            )
        }

        // Streaming Subscriptions Bottom Sheet
        if (uiState.isStreamingSubscriptionsSheetVisible) {
            StreamingSubscriptionsBottomSheet(
                onDismissRequest = { viewModel.closeStreamingSubscriptionsSheet() },
                onUpgradeToPro = {
                    viewModel.closeStreamingSubscriptionsSheet()
                    viewModel.openPaywall()
                }
            )
        }

        // Preferred Genres Bottom Sheet
        if (uiState.isPreferredGenresSheetVisible) {
            PreferredGenresBottomSheet(
                onDismissRequest = { viewModel.closePreferredGenresSheet() }
            )
        }

        // Pro Paywall Bottom Sheet
        if (uiState.isPaywallVisible) {
            ProPaywallBottomSheet(
                products = uiState.availableProducts,
                isProActive = uiState.isProActive,
                isRestoring = uiState.isRestoringPurchases,
                isPurchasing = uiState.isPurchasingProduct,
                errorMessage = uiState.paywallErrorMessage?.asString(context),
                onPurchaseClick = { act, product ->
                    viewModel.purchaseProduct(activity = act, product = product)
                },
                onRestoreClick = { viewModel.restorePurchases() },
                onDismissRequest = { viewModel.dismissPaywall() }
            )
        }

        // Developer / Debug Panel Bottom Sheet (Debug-Only)
        if (BuildConfig.DEBUG && uiState.isDeveloperPanelVisible) {
            DeveloperPanelBottomSheet(
                proOverride = uiState.proOverride,
                isMockTraktEnabled = uiState.isMockTraktEnabled,
                customTraktClientId = uiState.customTraktClientId,
                isAdsDisabled = uiState.isAdsDisabled,
                isTraktConnected = uiState.traktAuthState is TraktAuthState.Connected,
                onProOverrideSelected = { viewModel.setDebugProOverride(it) },
                onMockTraktToggled = { viewModel.setDebugMockTraktEnabled(it) },
                onSaveCustomTraktClientId = { viewModel.saveDebugCustomTraktClientId(it) },
                onAdsDisabledToggled = { viewModel.setDebugAdsDisabled(it) },
                onInstantMockConnectTrakt = { viewModel.instantMockConnectTrakt() },
                onDisconnectTrakt = { viewModel.disconnectTrakt() },
                onSeedFavorites = { viewModel.populateDemoFavorites() },
                onSeedWatchlist = { viewModel.populateDemoWatchlist() },
                onSeedHistory = { viewModel.populateDemoHistory() },
                onClearDatabase = { viewModel.clearLocalDatabase() },
                onResetCinemaGame = { viewModel.resetCinemaGame() },
                onResetAll = { viewModel.resetAllDebugOverrides() },
                onDismissRequest = { viewModel.dismissDeveloperPanel() }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    profile: Profile,
    isProActive: Boolean,
    isPaywallRemoteEnabled: Boolean,
    currentTheme: AppTheme,
    watchProviderRegion: String,
    availableRegions: List<WatchProviderRegion>,
    contentLanguage: String,
    availableLanguages: List<Language>,
    userStreamingSubscriptions: Set<Int>,
    userSeededGenres: Set<Int>,
    isReleaseRadarEnabled: Boolean,
    isReleaseRadarRemoteEnabled: Boolean,
    onReleaseRadarToggled: (Boolean) -> Unit,
    googleUser: GoogleUser?,
    backupStatus: BackupStatus,
    lastBackupMetadata: BackupMetadata?,
    guestPseudonym: String,
    isSigningIn: Boolean,
    isSigningOut: Boolean,
    traktAuthState: TraktAuthState,
    isMockTraktEnabled: Boolean,
    onUpgradeClick: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenStreamingSubscriptions: () -> Unit,
    onOpenPreferredGenres: () -> Unit,
    onOpenAbout: () -> Unit,
    onLogoutClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onOpenDeveloperPanelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGuest = profile.userName.equals("guest", ignoreCase = true) && googleUser == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Profile Avatar & Identity Card
        ProfileHeader(
            profile = profile,
            googleUser = googleUser,
            guestPseudonym = guestPseudonym,
            isProActive = isProActive,
            isGuest = isGuest,
            isSigningOut = isSigningOut,
            onLogoutClick = onLogoutClick
        )

        // Google Sign-In Prompt Card (shown only when not signed in)
        if (googleUser == null) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            GoogleSignInPromptCard(
                isSigningIn = isSigningIn,
                onSignInClick = onGoogleSignInClick
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Monetization Banners
        if (isProActive) {
            ProActiveBanner()
        } else if (isPaywallRemoteEnabled) {
            ProUpgradeBanner(onUpgradeClick = onUpgradeClick)
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Settings Navigation Group
        SettingsNavGroup(
            currentTheme = currentTheme,
            watchProviderRegion = watchProviderRegion,
            availableRegions = availableRegions,
            contentLanguage = contentLanguage,
            availableLanguages = availableLanguages,
            userStreamingSubscriptions = userStreamingSubscriptions,
            userSeededGenres = userSeededGenres,
            isReleaseRadarEnabled = isReleaseRadarEnabled,
            isReleaseRadarRemoteEnabled = isReleaseRadarRemoteEnabled,
            onReleaseRadarToggled = onReleaseRadarToggled,
            googleUser = googleUser,
            backupStatus = backupStatus,
            lastBackupMetadata = lastBackupMetadata,
            onOpenBackup = onOpenBackup,
            onOpenTheme = onOpenTheme,
            onOpenLocalization = onOpenLocalization,
            onOpenStreamingSubscriptions = onOpenStreamingSubscriptions,
            onOpenPreferredGenres = onOpenPreferredGenres,
            onOpenAbout = onOpenAbout,
            onOpenDeveloperPanelClick = onOpenDeveloperPanelClick
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}
