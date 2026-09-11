package com.ssverma.feature.library.ui.share.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.library.ListShareCardFormat
import com.ssverma.shared.domain.model.library.ListShareTheme

@Composable
internal fun SecretShareFormatSelector(
    selectedFormat: ListShareCardFormat,
    onSelectFormat: (ListShareCardFormat) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.secret_share_format_label) + ":",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilterChip(
            selected = selectedFormat == ListShareCardFormat.STORY_9_16,
            onClick = { onSelectFormat(ListShareCardFormat.STORY_9_16) },
            label = { Text(text = stringResource(R.string.secret_share_format_story)) }
        )
        FilterChip(
            selected = selectedFormat == ListShareCardFormat.SQUARE_1_1,
            onClick = { onSelectFormat(ListShareCardFormat.SQUARE_1_1) },
            label = { Text(text = stringResource(R.string.secret_share_format_square)) }
        )
    }
}

@Composable
internal fun SecretShareThemeSelector(
    selectedTheme: ListShareTheme,
    isLuxuryUnlocked: Boolean,
    onSelectTheme: (ListShareTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.secret_share_theme_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionChip(
                label = stringResource(R.string.secret_share_theme_classic),
                isSelected = selectedTheme == ListShareTheme.CLASSIC_SHOWTIME,
                isLocked = false,
                onClick = { onSelectTheme(ListShareTheme.CLASSIC_SHOWTIME) }
            )
            ThemeOptionChip(
                label = stringResource(R.string.secret_share_theme_vintage),
                isSelected = selectedTheme == ListShareTheme.VINTAGE_35MM,
                isLocked = !isLuxuryUnlocked,
                onClick = { onSelectTheme(ListShareTheme.VINTAGE_35MM) }
            )
            ThemeOptionChip(
                label = stringResource(R.string.secret_share_theme_oled),
                isSelected = selectedTheme == ListShareTheme.OLED_MIDNIGHT,
                isLocked = !isLuxuryUnlocked,
                onClick = { onSelectTheme(ListShareTheme.OLED_MIDNIGHT) }
            )
            ThemeOptionChip(
                label = stringResource(R.string.secret_share_theme_cyberpunk),
                isSelected = selectedTheme == ListShareTheme.NEON_CYBERPUNK,
                isLocked = !isLuxuryUnlocked,
                onClick = { onSelectTheme(ListShareTheme.NEON_CYBERPUNK) }
            )
        }
    }
}

@Composable
internal fun ThemeOptionChip(
    label: String,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label)
                if (isLocked) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors()
    )
}

@Composable
internal fun SecretShareCuratorCard(
    curatorName: String,
    onChangeNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.secret_share_sharing_as),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = curatorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(onClick = onChangeNameClick) {
                Text(
                    text = stringResource(R.string.secret_share_change_name),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun SecretShareCollaborativeToggleCard(
    isCollaborative: Boolean,
    onToggleCollaborative: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.secret_share_collaborative_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.secret_share_collaborative_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isCollaborative,
                onCheckedChange = onToggleCollaborative
            )
        }
    }
}
