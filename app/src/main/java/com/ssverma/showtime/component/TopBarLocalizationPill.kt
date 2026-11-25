package com.ssverma.showtime.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.region.iso31661ToFlagEmoji
import com.ssverma.showtime.R
import java.util.Locale

@Composable
internal fun TopBarLocalizationPill(
    watchProviderRegion: String,
    preferredOriginalLanguage: String,
    onLocalizationClick: () -> Unit,
    onResetLanguageFilter: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isLanguageFiltered = preferredOriginalLanguage.isNotBlank()
    val flagEmoji = remember(watchProviderRegion) {
        iso31661ToFlagEmoji(watchProviderRegion)
    }

    val languageDisplayName = remember(preferredOriginalLanguage) {
        if (preferredOriginalLanguage.isBlank()) {
            ""
        } else {
            try {
                val locale = Locale.forLanguageTag(preferredOriginalLanguage)
                val display = locale.getDisplayLanguage(Locale.ENGLISH)
                if (display.isNotBlank() && !display.equals(
                        preferredOriginalLanguage,
                        ignoreCase = true
                    )
                ) {
                    display.replaceFirstChar { it.uppercase() }
                } else {
                    preferredOriginalLanguage.uppercase()
                }
            } catch (_: Exception) {
                preferredOriginalLanguage.uppercase()
            }
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (isLanguageFiltered) {
            Surface(
                onClick = { menuExpanded = true },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ),
                modifier = Modifier.height(30.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = flagEmoji,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = preferredOriginalLanguage.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                offset = DpOffset(x = 0.dp, y = MaterialTheme.spacing.small),
                modifier = Modifier
                    .widthIn(min = 250.dp, max = 320.dp)
                    .padding(vertical = MaterialTheme.spacing.extraSmall)
            ) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.reset_to_global),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.reset_to_global_desc),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        menuExpanded = false
                        onResetLanguageFilter?.invoke()
                    },
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.small, vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.extraSmall
                    ),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.localization_settings),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val subtitleText = if (languageDisplayName.isNotBlank()) {
                                "$flagEmoji ${watchProviderRegion.uppercase()} · $languageDisplayName"
                            } else {
                                "$flagEmoji ${watchProviderRegion.uppercase()}"
                            }
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onLocalizationClick()
                    },
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.small, vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        } else {
            Surface(
                onClick = onLocalizationClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(30.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = flagEmoji,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (watchProviderRegion.isNotBlank()) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = watchProviderRegion.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
