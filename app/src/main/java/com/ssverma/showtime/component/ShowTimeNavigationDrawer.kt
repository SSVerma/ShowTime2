package com.ssverma.showtime.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLogo
import com.ssverma.showtime.BuildConfig
import com.ssverma.showtime.R

@Composable
fun ShowTimeDrawerContent(
    onOpenDiscovery: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenCinemaDiary: () -> Unit = {},
    onOpenTasteProfile: () -> Unit = {},
    onOpenWrapped: () -> Unit = {},
    onOpenBacklogChallenges: () -> Unit = {},
    onOpenCinemaGame: () -> Unit,
    onOpenReceipt: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Hero Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                ShowTimeLogo(
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Section 1: Explore & Discovery
            DrawerSectionHeader(title = stringResource(id = R.string.explore_section))
            DrawerItem(
                label = "Discover & Browse",
                icon = Icons.Rounded.AutoAwesome,
                onClick = onOpenDiscovery
            )
            DrawerItem(
                label = "Cinema Diary",
                icon = Icons.Rounded.Star,
                onClick = onOpenCinemaDiary
            )
            DrawerItem(
                label = "Taste Profile & Picks",
                icon = Icons.Rounded.AutoAwesome,
                onClick = onOpenTasteProfile
            )
            DrawerItem(
                label = "Cinema Wrapped & Milestones",
                icon = Icons.Rounded.EmojiEvents,
                onClick = onOpenWrapped
            )
            DrawerItem(
                label = "Blindspots & Challenges",
                icon = Icons.Rounded.EmojiEvents,
                onClick = onOpenBacklogChallenges
            )
            DrawerItem(
                label = stringResource(id = R.string.people),
                icon = Icons.Rounded.People,
                onClick = onOpenPeople
            )
            DrawerItem(
                label = stringResource(id = R.string.daily_cinema_challenge),
                icon = Icons.Rounded.Movie,
                onClick = onOpenCinemaGame
            )
            DrawerItem(
                label = stringResource(id = R.string.cinema_receipt),
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                onClick = onOpenReceipt
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Section 2: Sync & Storage
            DrawerSectionHeader(title = stringResource(id = R.string.sync_section))
            DrawerItem(
                label = stringResource(id = R.string.google_drive_backup),
                icon = Icons.Rounded.CloudSync,
                onClick = onOpenBackup
            )
            DrawerItem(
                label = stringResource(id = R.string.trakt_sync),
                icon = Icons.Rounded.Tv,
                onClick = onOpenTrakt
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Section 3: Preferences & Membership
            DrawerSectionHeader(title = stringResource(id = R.string.preferences_section))
            DrawerItem(
                label = stringResource(id = R.string.pro_membership),
                icon = Icons.Rounded.Star,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onOpenPro
            )
            DrawerItem(
                label = stringResource(id = R.string.appearance_theme),
                icon = Icons.Rounded.Palette,
                onClick = onOpenTheme
            )
            DrawerItem(
                label = stringResource(id = R.string.localization_settings),
                icon = Icons.Rounded.Public,
                onClick = onOpenLocalization
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Section 4: About & Legal
            DrawerSectionHeader(title = stringResource(id = R.string.legal_section))
            DrawerItem(
                label = stringResource(id = R.string.privacy_policy),
                icon = Icons.Rounded.Policy,
                onClick = onOpenPrivacy
            )
            DrawerItem(
                label = stringResource(id = R.string.open_source_licenses),
                icon = Icons.Rounded.Description,
                onClick = onOpenLicenses
            )
            DrawerItem(
                label = stringResource(id = R.string.about_showtime),
                icon = Icons.Rounded.Info,
                onClick = onOpenAbout
            )
        }
    }
}

@Composable
private fun DrawerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    NavigationDrawerItem(
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = iconTint
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
