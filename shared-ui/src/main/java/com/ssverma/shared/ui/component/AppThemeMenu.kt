package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.R

@Composable
fun AppThemeMenu(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    isDynamicColorEnabled: Boolean,
    onDynamicColorToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = stringResource(id = R.string.theme)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppTheme.entries.forEach { theme ->
                DropdownMenuItem(
                    text = {
                        Text(text = theme.name)
                    },
                    trailingIcon = {
                        if (currentTheme == theme) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    }
                )
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.extraSmall))

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.dynamic_color),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(id = R.string.dynamic_color_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                            Switch(
                                checked = isDynamicColorEnabled,
                                onCheckedChange = { onDynamicColorToggled(it) },
                                thumbContent = if (isDynamicColorEnabled) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize)
                                        )
                                    }
                                } else null
                            )
                        }
                    },
                    onClick = { /* No-op, switch handles it */ }
                )
            }
        }
    }
}
