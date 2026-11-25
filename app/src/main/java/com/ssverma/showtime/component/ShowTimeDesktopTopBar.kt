package com.ssverma.showtime.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.layout.windowTopBarInsetsPadding
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.ProfileAvatarSharedKey
import com.ssverma.showtime.R

/**
 * Persistent top command bar rendered above workspace panes on Medium and Large viewports.
 */
@Composable
fun ShowTimeDesktopTopBar(
    googleUser: GoogleUser?,
    isProActive: Boolean,
    watchProviderRegion: String,
    preferredOriginalLanguage: String,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLocalizationClick: () -> Unit,
    onResetLanguageFilter: () -> Unit,
    onOpenKeyboardShortcuts: () -> Unit,
    modifier: Modifier = Modifier,
    showSearchTrigger: Boolean = true
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxWidth()
            .windowTopBarInsetsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesktopTopBarDefaults.Height)
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                if (showSearchTrigger) {
                    Surface(
                        onClick = onSearchClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .widthIn(
                                min = DesktopTopBarDefaults.SearchMinWidth,
                                max = DesktopTopBarDefaults.SearchMaxWidth
                            )
                            .height(DesktopTopBarDefaults.SearchHeight)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.medium)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                            Text(
                                text = stringResource(id = R.string.search_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = stringResource(id = R.string.search_shortcut_hint),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onOpenKeyboardShortcuts,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Keyboard,
                        contentDescription = stringResource(id = R.string.keyboard_shortcuts_title),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                TopBarLocalizationPill(
                    watchProviderRegion = watchProviderRegion,
                    preferredOriginalLanguage = preferredOriginalLanguage,
                    onLocalizationClick = onLocalizationClick,
                    onResetLanguageFilter = onResetLanguageFilter
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                if (isProActive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = stringResource(id = R.string.pro_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Avatar(
                    imageUrl = googleUser?.photoUrl.orEmpty(),
                    contentDescription = googleUser?.displayName
                        ?: stringResource(id = R.string.account),
                    onClick = onProfileClick,
                    size = 36.dp,
                    borderWidth = 1.dp,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    borderSpacing = 0.dp,
                    enableSharedTransition = true,
                    sharedContentKey = ProfileAvatarSharedKey,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

private object DesktopTopBarDefaults {
    val Height = 64.dp
    val SearchHeight = 44.dp
    val SearchMinWidth = 260.dp
    val SearchMaxWidth = 520.dp
}
