package com.ssverma.feature.account.ui.profile

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
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
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.ui.component.Avatar
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
                    traktAuthState = uiState.traktAuthState,
                    onUpgradeClick = { viewModel.openPaywall() },
                    onOpenBackup = onOpenBackup,
                    onOpenTrakt = onOpenTrakt,
                    onOpenTheme = { viewModel.openThemeSheet() },
                    onOpenLocalization = { viewModel.openLocalizationSheet() },
                    onOpenAbout = onOpenAbout,
                    onLogoutClick = { viewModel.logout() },
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
            com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet(
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
                onSeedFavorites = { viewModel.seedSampleFavorites() },
                onSeedWatchlist = { viewModel.seedSampleWatchlist() },
                onSeedHistory = { viewModel.seedSampleHistory() },
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
    availableLanguages: List<com.ssverma.shared.domain.model.Language>,
    googleUser: com.ssverma.core.backup.model.GoogleUser?,
    traktAuthState: TraktAuthState,
    onUpgradeClick: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenAbout: () -> Unit,
    onLogoutClick: () -> Unit,
    onOpenDeveloperPanelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGuest = profile.userName.equals("guest", ignoreCase = true)

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
            onLogoutClick = onLogoutClick
        )

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
    googleUser: com.ssverma.core.backup.model.GoogleUser?,
    isProActive: Boolean,
    isGuest: Boolean,
    onLogoutClick: () -> Unit
) {
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
        text = profile.displayName.ifBlank { profile.userName },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isProActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                if (isProActive) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.showtime_pro),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = if (isGuest) stringResource(R.string.guest) else profile.userName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isGuest) {
            OutlinedButton(
                onClick = onLogoutClick,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun SettingsNavGroup(
    currentTheme: AppTheme,
    watchProviderRegion: String,
    availableRegions: List<WatchProviderRegion>,
    contentLanguage: String,
    availableLanguages: List<com.ssverma.shared.domain.model.Language>,
    googleUser: com.ssverma.core.backup.model.GoogleUser?,
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
