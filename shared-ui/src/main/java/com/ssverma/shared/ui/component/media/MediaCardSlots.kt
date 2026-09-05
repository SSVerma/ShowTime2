package com.ssverma.shared.ui.component.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.shared.ui.R
import java.util.Locale

@Composable
fun MediaCardRatingBadge(
    rating: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.75f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = String.format(Locale.getDefault(), "%.1f", rating),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun MediaCardWatchedBadge(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiary,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.media_card_badge_watched),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

@Composable
fun MediaCardFrostedActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    containerColor: Color = Color.Transparent,
    size: Int = 32,
    showActiveDot: Boolean = false,
    activeDotColor: Color = MaterialTheme.colorScheme.primary,
    hasShadow: Boolean = true
) {
    val iconSize = 20.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .then(
                if (hasShadow) {
                    Modifier.drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                radius = size.dp.toPx() * 0.75f
                            )
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Drop shadow layer for the icon when on poster
                if (hasShadow) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .size(iconSize)
                            .offset(x = 0.dp, y = 1.dp)
                    )
                }

                // Foreground icon
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Active dot indicator (snugly badged at top-end of the icon)
        AnimatedVisibility(
            visible = showActiveDot,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(modifier = Modifier.size(iconSize)) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 1.dp, y = 1.dp)
                        .shadow(elevation = 2.dp, shape = CircleShape)
                        .background(
                            color = activeDotColor,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.2.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun MediaCardOverflowAction(
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showActiveDot: Boolean = false,
    isOverPoster: Boolean = true,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        MediaCardFrostedActionButton(
            icon = Icons.Rounded.MoreVert,
            contentDescription = stringResource(R.string.media_card_overflow_cd),
            onClick = onToggleExpand,
            showActiveDot = showActiveDot,
            tint = if (isOverPoster) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            hasShadow = isOverPoster
        )

        val scrollState = rememberScrollState()
        LaunchedEffect(expanded) {
            if (expanded) {
                scrollState.scrollTo(0)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            scrollState = scrollState,
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .widthIn(min = 220.dp, max = 280.dp)
                .heightIn(max = 380.dp),
            content = menuContent
        )
    }
}

@Composable
fun MediaCardFavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(24.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(R.string.media_card_favorite_cd),
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
            modifier = Modifier.size(16.dp)
        )
    }
}
