package com.ssverma.showtime.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.ProfileAvatarSharedKey
import com.ssverma.showtime.R

@Composable
fun ShowTimeTopSearchBar(
    googleUser: GoogleUser?,
    isProActive: Boolean,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .height(52.dp)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                clip = false
            )
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            ) {
                // Leading Menu Button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = stringResource(id = R.string.open_navigation_drawer),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Search Icon & Placeholder (Interactive Search Target)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onSearchClick
                        )
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(id = R.string.search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isProActive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.pro_badge),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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
                    sharedContentKey = ProfileAvatarSharedKey
                )
            }
        }
    }
}
