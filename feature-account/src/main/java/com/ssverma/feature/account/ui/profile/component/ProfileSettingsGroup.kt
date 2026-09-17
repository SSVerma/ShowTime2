package com.ssverma.feature.account.ui.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.BuildConfig
import com.ssverma.feature.account.R
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion

@Composable
fun SettingsNavGroup(
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
    onOpenBackup: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenLocalization: () -> Unit,
    onOpenStreamingSubscriptions: () -> Unit,
    onOpenPreferredGenres: () -> Unit,
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

        val backupSubtitle = when {
            googleUser == null -> stringResource(R.string.cloud_backup_desc)
            backupStatus is BackupStatus.InProgress -> {
                if (backupStatus.operation == BackupOperation.RESTORE) {
                    stringResource(R.string.cloud_backup_status_restoring)
                } else {
                    stringResource(R.string.cloud_backup_status_syncing)
                }
            }

            lastBackupMetadata != null -> stringResource(
                R.string.cloud_backup_status_last_backup,
                lastBackupMetadata.formattedDate
            )

            else -> stringResource(R.string.cloud_backup_status_no_backup)
        }

        SettingsNavTile(
            title = stringResource(R.string.cloud_backup),
            subtitle = backupSubtitle,
            icon = Icons.Rounded.CloudSync,
            onClick = onOpenBackup
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Section 2: Preferences & Customization
        Text(
            text = stringResource(R.string.preferences_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall)
        )

        val themeSubtitle = when (currentTheme) {
            AppTheme.System -> stringResource(R.string.theme_system)
            AppTheme.Light -> stringResource(R.string.theme_light)
            AppTheme.Dark -> stringResource(R.string.theme_dark)
            AppTheme.OledMidnight -> stringResource(R.string.theme_oled)
        }
        SettingsNavTile(
            title = stringResource(R.string.appearance),
            subtitle = themeSubtitle,
            icon = Icons.Rounded.Palette,
            onClick = onOpenTheme
        )

        val currentRegionName =
            availableRegions.find { it.iso31661 == watchProviderRegion }?.englishName
                ?: watchProviderRegion
        val currentLanguageName = availableLanguages.find { it.iso6391 == contentLanguage }?.let {
            if (it.englishName != it.name && it.name.isNotBlank()) {
                "${it.englishName} (${it.name})"
            } else {
                it.englishName
            }
        } ?: contentLanguage
        SettingsNavTile(
            title = stringResource(R.string.localization_settings),
            subtitle = "$currentRegionName • $currentLanguageName",
            icon = Icons.Rounded.Public,
            onClick = onOpenLocalization
        )

        val streamingSubtitle = if (userStreamingSubscriptions.isEmpty()) {
            stringResource(R.string.streaming_subscriptions_select_hint)
        } else {
            stringResource(
                R.string.streaming_subscriptions_count_format,
                userStreamingSubscriptions.size
            )
        }
        SettingsNavTile(
            title = stringResource(R.string.streaming_subscriptions_title),
            subtitle = streamingSubtitle,
            icon = Icons.Rounded.LiveTv,
            onClick = onOpenStreamingSubscriptions
        )

        val genresSubtitle = if (userSeededGenres.isEmpty()) {
            stringResource(R.string.preferred_genres_select_hint)
        } else {
            stringResource(
                R.string.preferred_genres_count_format,
                userSeededGenres.size
            )
        }
        SettingsNavTile(
            title = stringResource(R.string.preferred_genres_title),
            subtitle = genresSubtitle,
            icon = Icons.Rounded.MovieFilter,
            onClick = onOpenPreferredGenres
        )

        if (isReleaseRadarRemoteEnabled) {
            SettingsSwitchTile(
                title = stringResource(R.string.settings_release_radar_title),
                subtitle = stringResource(R.string.settings_release_radar_desc),
                icon = Icons.Rounded.NotificationsActive,
                checked = isReleaseRadarEnabled,
                onCheckedChange = onReleaseRadarToggled
            )
        }

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
