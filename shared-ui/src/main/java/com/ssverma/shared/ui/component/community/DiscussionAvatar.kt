package com.ssverma.shared.ui.component.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Curated, expressive palette providing distinct, vibrant character identity
 * while remaining harmonious with light & dark theme backgrounds.
 */
@Composable
fun getM3AvatarColors(key: String, isOwner: Boolean = false): Pair<Color, Color> {
    val colorScheme = MaterialTheme.colorScheme
    if (isOwner) {
        return colorScheme.primary to colorScheme.onPrimary
    }

    val themeColorPairs = listOf(
        colorScheme.primaryContainer to colorScheme.onPrimaryContainer,
        colorScheme.secondaryContainer to colorScheme.onSecondaryContainer,
        colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer,
        colorScheme.secondary to colorScheme.onSecondary,
        colorScheme.tertiary to colorScheme.onTertiary,
        colorScheme.surfaceVariant to colorScheme.onSurfaceVariant,
        colorScheme.inverseSurface to colorScheme.inverseOnSurface
    )
    val index = kotlin.math.abs(key.hashCode()) % themeColorPairs.size
    return themeColorPairs[index]
}

@Composable
fun DiscussionAvatar(
    authorId: String,
    authorName: String,
    avatarInitial: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    isOwner: Boolean = false,
    size: Dp = 36.dp
) {
    val (containerColor, contentColor) = getM3AvatarColors(key = authorId, isOwner = isOwner)

    val borderModifier = if (isOwner) {
        Modifier.border(
            border = BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            ),
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size = size)
            .then(other = borderModifier)
            .clip(shape = CircleShape)
            .background(color = containerColor)
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = authorName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size = size)
                    .clip(shape = CircleShape)
            )
        } else {
            Text(
                text = avatarInitial,
                style = if (size < 32.dp) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun DiscussionAvatarWithSpine(
    authorId: String,
    authorName: String,
    avatarInitial: String,
    avatarUrl: String?,
    showSpine: Boolean,
    modifier: Modifier = Modifier,
    isOwner: Boolean = false,
    size: Dp = 36.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(width = size)
            .fillMaxHeight()
    ) {
        DiscussionAvatar(
            authorId = authorId,
            authorName = authorName,
            avatarInitial = avatarInitial,
            avatarUrl = avatarUrl,
            isOwner = isOwner,
            size = size
        )

        if (showSpine) {
            Spacer(modifier = Modifier.height(height = 4.dp))
            Box(
                modifier = Modifier
                    .width(width = 2.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
            )
        }
    }
}
