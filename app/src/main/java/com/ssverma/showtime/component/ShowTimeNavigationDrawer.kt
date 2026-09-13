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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.TaskAlt
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
    onOpenMovieMatch: () -> Unit,
    onOpenPeople: () -> Unit,
    onOpenCinemaDiary: () -> Unit = {},
    onOpenTasteProfile: () -> Unit = {},
    onOpenWrapped: () -> Unit = {},
    onOpenBacklogChallenges: () -> Unit = {},
    onOpenReceipt: () -> Unit,
    onOpenCinemaGame: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenTrakt: () -> Unit = {},
    onOpenPro: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenWhatsNew: () -> Unit = {},
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

            DrawerDivider()

            // Section 1: Explore & Social
            DrawerSectionHeader(title = stringResource(id = R.string.drawer_section_explore))
            DrawerItem(
                label = stringResource(id = R.string.drawer_discover),
                icon = Icons.Rounded.Explore,
                onClick = onOpenDiscovery
            )
            DrawerItem(
                label = stringResource(id = R.string.drawer_movie_match),
                icon = Icons.Rounded.Favorite,
                onClick = onOpenMovieMatch
            )
            DrawerItem(
                label = stringResource(id = R.string.people),
                icon = Icons.Rounded.People,
                onClick = onOpenPeople
            )

            DrawerDivider()

            // Section 2: Cinephile Hub
            DrawerSectionHeader(title = stringResource(id = R.string.drawer_section_cinephile))
            DrawerItem(
                label = stringResource(id = R.string.drawer_cinema_diary),
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                onClick = onOpenCinemaDiary
            )
            DrawerItem(
                label = stringResource(id = R.string.drawer_taste_profile),
                icon = Icons.Rounded.Psychology,
                onClick = onOpenTasteProfile
            )
            DrawerItem(
                label = stringResource(id = R.string.drawer_wrapped),
                icon = Icons.Rounded.EmojiEvents,
                onClick = onOpenWrapped
            )
            DrawerItem(
                label = stringResource(id = R.string.drawer_challenges),
                icon = Icons.Rounded.TaskAlt,
                onClick = onOpenBacklogChallenges
            )
            DrawerItem(
                label = stringResource(id = R.string.cinema_receipt),
                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                onClick = onOpenReceipt
            )

            DrawerDivider()

            // Section 3: Daily Habit
            DrawerSectionHeader(title = stringResource(id = R.string.drawer_section_daily))
            DrawerItem(
                label = stringResource(id = R.string.daily_cinema_challenge),
                icon = Icons.Rounded.Extension,
                onClick = onOpenCinemaGame
            )

            DrawerDivider()

            // Section 4: Sync & Backup
            DrawerSectionHeader(title = stringResource(id = R.string.sync_section))
            DrawerItem(
                label = stringResource(id = R.string.google_drive_backup),
                icon = Icons.Rounded.CloudSync,
                onClick = onOpenBackup
            )

            DrawerDivider()

            // Section 5: Preferences & Membership
            DrawerSectionHeader(title = stringResource(id = R.string.preferences_section))
            DrawerItem(
                label = stringResource(id = R.string.pro_membership),
                icon = Icons.Rounded.Diamond,
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

            DrawerDivider()

            // Section 6: About & Legal
            DrawerSectionHeader(title = stringResource(id = R.string.legal_section))
            DrawerItem(
                label = stringResource(id = R.string.drawer_whats_new),
                icon = Icons.Rounded.NewReleases,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onOpenWhatsNew
            )
            DrawerItem(
                label = stringResource(id = R.string.about_showtime),
                icon = Icons.Rounded.Info,
                onClick = onOpenAbout
            )
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
private fun DrawerDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
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
