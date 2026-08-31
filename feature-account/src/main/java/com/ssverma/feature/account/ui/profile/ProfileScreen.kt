package com.ssverma.feature.account.ui.profile

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.BuildConfig
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.ui.debug.DeveloperPanelBottomSheet
import com.ssverma.feature.account.ui.pro.ProPaywallBottomSheet
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet
import com.ssverma.shared.ui.component.ProfileAvatarSharedKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
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
                    googleUser = uiState.googleUser,
                    isSigningIn = uiState.isSigningIn,
                    isSigningOut = uiState.isSigningOut,
                    traktAuthState = uiState.traktAuthState,
                    onUpgradeClick = { viewModel.openPaywall() },
                    onOpenBackup = onOpenBackup,
                    onOpenTrakt = onOpenTrakt,
                    onOpenTheme = { viewModel.openThemeSheet() },
                    onOpenLocalization = { viewModel.openLocalizationSheet() },
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
            AlertDialog(
                onDismissRequest = { showSignOutConfirmDialog = false },
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.sign_out_confirm_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(R.string.sign_out_confirm_msg))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSignOutConfirmDialog = false
                            viewModel.signOutGoogle()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = stringResource(R.string.sign_out))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutConfirmDialog = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
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

        // Pro Paywall Bottom Sheet
        if (uiState.isPaywallVisible) {
            ProPaywallBottomSheet(
                products = uiState.availableProducts,
                isProActive = uiState.isProActive,
                isRestoring = uiState.isRestoringPurchases,
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
    googleUser: GoogleUser?,
    isSigningIn: Boolean,
    isSigningOut: Boolean,
    traktAuthState: TraktAuthState,
    onUpgradeClick: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
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

        // Avatar & Info Header
        ProfileHeader(
            profile = profile,
            googleUser = googleUser,
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

        // Pro Status / Upgrade Banner
        if (isProActive) {
            ProActiveBanner()
        } else if (isPaywallRemoteEnabled) {
            ProUpgradeBanner(onUpgradeClick = onUpgradeClick)
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Settings Navigation Group
        SettingsNavGroup(
            currentTheme = currentTheme,
            watchProviderRegion = watchProviderRegion,
            availableRegions = availableRegions,
            contentLanguage = contentLanguage,
            availableLanguages = availableLanguages,
            googleUser = googleUser,
            traktAuthState = traktAuthState,
            onOpenBackup = onOpenBackup,
            onOpenTrakt = onOpenTrakt,
            onOpenTheme = onOpenTheme,
            onOpenLocalization = onOpenLocalization,
            onOpenAbout = onOpenAbout,
            onOpenDeveloperPanelClick = onOpenDeveloperPanelClick
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun ProfileHeader(
    profile: Profile,
    googleUser: GoogleUser?,
    isProActive: Boolean,
    isGuest: Boolean,
    isSigningOut: Boolean,
    onLogoutClick: () -> Unit
) {
    val displayName = googleUser?.displayName?.takeIf { it.isNotBlank() }
        ?: profile.displayName.ifBlank { profile.userName }
    val avatarUrl = googleUser?.photoUrl?.toString()?.ifBlank { null } ?: profile.imageUrl

    Avatar(
        imageUrl = avatarUrl,
        onClick = {},
        size = 96.dp,
        enableSharedTransition = true,
        sharedContentKey = ProfileAvatarSharedKey
    )

    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

    Text(
        text = displayName,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    if (googleUser != null && googleUser.email.isNotBlank()) {
        Text(
            text = googleUser.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = Modifier.padding(top = MaterialTheme.spacing.small)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isProActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (isProActive) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.showtime_pro),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else if (isGuest) {
                    Text(
                        text = stringResource(R.string.guest),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.google_account),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isGuest) {
            OutlinedButton(
                onClick = onLogoutClick,
                enabled = !isSigningOut,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                if (isSigningOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.signing_out),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_out),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInPromptCard(
    isSigningIn: Boolean,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stringResource(R.string.connect_google_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.connect_google_account_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
            ) {
                BenefitBulletItem(
                    icon = Icons.Rounded.CloudSync,
                    text = stringResource(R.string.sync_benefit_backup)
                )
                BenefitBulletItem(
                    icon = Icons.Rounded.Public,
                    text = stringResource(R.string.sync_benefit_community)
                )
                BenefitBulletItem(
                    icon = Icons.Rounded.Tv,
                    text = stringResource(R.string.sync_benefit_devices)
                )
            }

            Button(
                onClick = onSignInClick,
                enabled = !isSigningIn,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.signing_in),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sign_in_with_google),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitBulletItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsNavGroup(
    currentTheme: AppTheme,
    watchProviderRegion: String,
    availableRegions: List<WatchProviderRegion>,
    contentLanguage: String,
    availableLanguages: List<Language>,
    googleUser: GoogleUser?,
    traktAuthState: TraktAuthState,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDeveloperPanelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier.fillMaxWidth()
    ) {
        // Section 1: Sync & Storage
        Text(
            text = stringResource(R.string.sync_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall)
        )

        SettingsNavTile(
            title = stringResource(R.string.cloud_backup),
            subtitle = if (googleUser != null) {
                googleUser.email
            } else {
                stringResource(R.string.cloud_backup_desc)
            },
            icon = Icons.Rounded.CloudSync,
            onClick = onOpenBackup
        )

        SettingsNavTile(
            title = stringResource(R.string.trakt_cloud_sync),
            subtitle = when (traktAuthState) {
                is TraktAuthState.Connected -> stringResource(
                    id = R.string.trakt_connected_as,
                    traktAuthState.user.username
                )

                else -> stringResource(R.string.trakt_cloud_sync_desc)
            },
            icon = Icons.Rounded.Tv,
            onClick = onOpenTrakt
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Section 2: Preferences
        Text(
            text = stringResource(R.string.preferences_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall)
        )

        val themeTitle = when (currentTheme) {
            AppTheme.System -> stringResource(R.string.theme_system)
            AppTheme.Light -> stringResource(R.string.theme_light)
            AppTheme.Dark -> stringResource(R.string.theme_dark)
            AppTheme.OledMidnight -> stringResource(R.string.theme_oled)
        }
        SettingsNavTile(
            title = stringResource(R.string.appearance),
            subtitle = themeTitle,
            icon = Icons.Rounded.Palette,
            onClick = onOpenTheme
        )

        val currentRegionName =
            availableRegions.find { it.iso31661.equals(watchProviderRegion, ignoreCase = true) }
                ?.let {
                    "${it.englishName} (${it.iso31661})"
                } ?: watchProviderRegion
        val currentLanguageName =
            availableLanguages.find { it.iso6391.equals(contentLanguage, ignoreCase = true) }?.let {
                "${it.englishName} (${it.iso6391})"
            } ?: contentLanguage
        SettingsNavTile(
            title = stringResource(R.string.localization_settings),
            subtitle = "$currentRegionName • $currentLanguageName",
            icon = Icons.Rounded.Public,
            onClick = onOpenLocalization
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Section 3: About & Advanced
        Text(
            text = stringResource(R.string.about_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall)
        )

        SettingsNavTile(
            title = stringResource(R.string.about_showtime),
            subtitle = stringResource(R.string.about_showtime_desc),
            icon = Icons.Rounded.Info,
            onClick = onOpenAbout
        )

        if (BuildConfig.DEBUG) {
            SettingsNavTile(
                title = stringResource(R.string.developer_controls),
                subtitle = stringResource(R.string.developer_controls_desc),
                icon = Icons.Rounded.BugReport,
                onClick = onOpenDeveloperPanelClick
            )
        }
    }
}

@Composable
private fun SettingsNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProActiveBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                ) {
                    Text(
                        text = stringResource(R.string.showtime_pro),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.pro_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProUpgradeBanner(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onUpgradeClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            // Header Row: Icon + Title/Subtitle + PRO Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        Text(
                            text = stringResource(R.string.showtime_pro),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.pro_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Feature Highlights Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                ProFeatureBadge(
                    icon = Icons.Rounded.Block,
                    text = "100% Ad-Free",
                    modifier = Modifier.weight(1f)
                )
                ProFeatureBadge(
                    icon = Icons.Rounded.DarkMode,
                    text = "OLED Black",
                    modifier = Modifier.weight(1f)
                )
                ProFeatureBadge(
                    icon = Icons.Rounded.CloudSync,
                    text = "Trakt Sync",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Primary Call-to-Action
            Button(
                onClick = onUpgradeClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(R.string.upgrade_to_pro),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProFeatureBadge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
