package com.ssverma.showtime.component

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R

@Immutable
private data class ShortcutLegendItem(
    val keysLabel: String,
    val description: String
)

/**
 * Cheat-sheet modal showing all desktop keyboard shortcuts available in ShowTime.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardShortcutsDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcutItems = listOf(
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_ctrl_k),
            description = stringResource(R.string.shortcut_open_search)
        ),
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_alt_digits),
            description = stringResource(R.string.shortcut_switch_tabs)
        ),
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_alt_d),
            description = stringResource(R.string.shortcut_open_diary)
        ),
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_alt_u),
            description = stringResource(R.string.shortcut_open_discover)
        ),
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_help),
            description = stringResource(R.string.shortcut_show_help)
        ),
        ShortcutLegendItem(
            keysLabel = stringResource(R.string.shortcut_key_esc),
            description = stringResource(R.string.shortcut_go_back)
        )
    )

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.large)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Keyboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(MaterialTheme.spacing.small)
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.keyboard_shortcuts_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.keyboard_shortcuts_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            shortcutItems.forEach { item ->
                ShortcutLegendRow(item = item)
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun ShortcutLegendRow(
    item: ShortcutLegendItem,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small)
    ) {
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = MaterialTheme.spacing.medium)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        ) {
            Text(
                text = item.keysLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.smallMedium,
                    vertical = MaterialTheme.spacing.extraSmall
                )
            )
        }
    }
}
