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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.ui.pro.ProPaywallBottomSheet
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.component.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
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
                ProfileMainContent(
                    profile = content.profile,
                    isProActive = uiState.isProActive,
                    isPaywallRemoteEnabled = uiState.isPaywallRemoteEnabled,
                    currentTheme = uiState.currentTheme,
                    googleUser = uiState.googleUser,
                    backupStatus = uiState.backupStatus,
                    lastBackupMetadata = uiState.lastBackupMetadata,
                    onUpgradeClick = { viewModel.openPaywall() },
                    onThemeSelected = { viewModel.updateTheme(it) },
                    onSignInGoogleClick = {
                        activity?.let { viewModel.signInWithGoogle(it) }
                    },
                    onSignOutGoogleClick = { showSignOutConfirmDialog = true },
                    onBackupNowClick = { viewModel.backupNow() },
                    onRestoreBackupClick = { showRestoreConfirmDialog = true },
                    onLoginClick = onLoginClick,
                    onLogoutClick = { viewModel.logout() },
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

        // Restore Backup Confirmation Dialog
        if (showRestoreConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreConfirmDialog = false },
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.restore_backup_confirm_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(R.string.restore_backup_confirm_msg))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRestoreConfirmDialog = false
                            viewModel.restoreBackup()
                        }
                    ) {
                        Text(text = stringResource(R.string.restore_backup))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreConfirmDialog = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
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
                        Text(text = stringResource(R.string.sign_out_google))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSignOutConfirmDialog = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
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
    }
}

@Composable
private fun ProfileMainContent(
    profile: Profile,
    isProActive: Boolean,
    isPaywallRemoteEnabled: Boolean,
    currentTheme: AppTheme,
    googleUser: GoogleUser?,
    backupStatus: BackupStatus,
    lastBackupMetadata: BackupMetadata?,
    onUpgradeClick: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onSignInGoogleClick: () -> Unit,
    onSignOutGoogleClick: () -> Unit,
    onBackupNowClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
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

        // User Avatar & Name
        Avatar(
            imageUrl = profile.imageUrl,
            onClick = {},
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = profile.displayName.ifBlank { profile.userName },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = MaterialTheme.spacing.small)
        )

        Text(
            text = if (isGuest) stringResource(R.string.guest) else stringResource(
                id = R.string.username_n,
                profile.userName
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // ShowTime Pro Status / Upgrade Banner
        if (isProActive) {
            ProActiveBanner()
        } else if (isPaywallRemoteEnabled) {
            ProUpgradeBanner(onUpgradeClick = onUpgradeClick)
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Cloud Backup Section
        CloudBackupCard(
            googleUser = googleUser,
            backupStatus = backupStatus,
            lastBackupMetadata = lastBackupMetadata,
            onSignInGoogleClick = onSignInGoogleClick,
            onSignOutGoogleClick = onSignOutGoogleClick,
            onBackupNowClick = onBackupNowClick,
            onRestoreBackupClick = onRestoreBackupClick
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Settings / Appearance Section
        SettingsSectionHeader(
            icon = Icons.Rounded.Palette,
            title = stringResource(R.string.appearance)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Theme Switcher Card
        ThemeSelectorCard(
            currentTheme = currentTheme,
            isProActive = isProActive,
            onThemeSelected = { selectedTheme ->
                if (selectedTheme == AppTheme.OledMidnight && !isProActive) {
                    onUpgradeClick()
                } else {
                    onThemeSelected(selectedTheme)
                }
            }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Auth Action: Connect TMDB for Guest vs Logout for Authenticated User
        if (isGuest) {
            Button(
                onClick = onLoginClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.connect_tmdb),
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            OutlinedButton(
                onClick = onLogoutClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.logout))
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun ProActiveBanner(
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
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
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
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
                Text(
                    text = stringResource(R.string.pro_active),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF9800).copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Column {
                    Text(
                        text = stringResource(R.string.showtime_pro),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.pro_feature_no_ads_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Button(
                onClick = onUpgradeClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.unlock_showtime_pro),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.extraSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeSelectorCard(
    currentTheme: AppTheme,
    isProActive: Boolean,
    onThemeSelected: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Row 1: System & Light
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeOptionButton(
                    title = stringResource(R.string.theme_system),
                    selected = currentTheme == AppTheme.System,
                    onClick = { onThemeSelected(AppTheme.System) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = stringResource(R.string.theme_light),
                    selected = currentTheme == AppTheme.Light,
                    onClick = { onThemeSelected(AppTheme.Light) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Row 2: Dark & OLED Midnight
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeOptionButton(
                    title = stringResource(R.string.theme_dark),
                    selected = currentTheme == AppTheme.Dark,
                    onClick = { onThemeSelected(AppTheme.Dark) },
                    modifier = Modifier.weight(1f)
                )
                ThemeOptionButton(
                    title = stringResource(R.string.theme_oled),
                    selected = currentTheme == AppTheme.OledMidnight,
                    isPro = !isProActive,
                    onClick = { onThemeSelected(AppTheme.OledMidnight) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPro: Boolean = false
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        modifier = modifier.height(44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.small)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
            if (isPro) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Pro",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CloudBackupCard(
    googleUser: GoogleUser?,
    backupStatus: BackupStatus,
    lastBackupMetadata: BackupMetadata?,
    onSignInGoogleClick: () -> Unit,
    onSignOutGoogleClick: () -> Unit,
    onBackupNowClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.cloud_backup),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.cloud_backup_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            if (googleUser == null) {
                Button(
                    onClick = onSignInGoogleClick,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = stringResource(R.string.sign_in_with_google),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.extraSmall)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = googleUser.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = googleUser.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = googleUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onSignOutGoogleClick) {
                        Text(
                            text = "Sign out",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                val isBackingUp =
                    backupStatus is BackupStatus.InProgress && backupStatus.operation == BackupOperation.BACKUP
                val isRestoring =
                    backupStatus is BackupStatus.InProgress && backupStatus.operation == BackupOperation.RESTORE

                if (lastBackupMetadata != null) {
                    Text(
                        text = stringResource(
                            R.string.last_backup,
                            lastBackupMetadata.formattedDate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${
                            stringResource(
                                R.string.backup_size,
                                lastBackupMetadata.formattedSize
                            )
                        } • ${lastBackupMetadata.favoritesCount} Favorites • ${lastBackupMetadata.watchlistCount} Watchlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.last_backup, "Never"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onBackupNowClick,
                        enabled = !isBackingUp && !isRestoring,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(text = stringResource(R.string.backing_up))
                        } else {
                            Text(
                                text = stringResource(R.string.back_up_now),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRestoreBackupClick,
                        enabled = !isBackingUp && !isRestoring && lastBackupMetadata != null,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(text = stringResource(R.string.restoring))
                        } else {
                            Text(
                                text = stringResource(R.string.restore_backup),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

