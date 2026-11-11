package com.ssverma.common.ui.a2ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiComponent

/**
 * Renders an interactive movie poster card emitted by the AI Agent.
 */
@Composable
fun A2UiMediaCardRenderer(
    mediaCard: A2UiComponent.MediaCard,
    onAction: (A2UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val posterPath = mediaCard.posterPath?.trim()

    val fullPosterUrl = when {
        posterPath.isNullOrBlank() ||
                posterPath.equals("null", ignoreCase = true) ||
                posterPath.equals("n/a", ignoreCase = true) -> null

        posterPath.startsWith("http://") || posterPath.startsWith("https://") -> posterPath
        posterPath.startsWith("/") -> "https://image.tmdb.org/t/p/w500$posterPath"
        else -> "https://image.tmdb.org/t/p/w500/$posterPath"
    }

    ElevatedCard(
        onClick = {
            onAction(
                A2UiAction.OpenMedia(
                    tmdbId = mediaCard.tmdbId,
                    mediaType = "movie",
                ),
            )
        },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(108.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                if (fullPosterUrl != null) {
                    NetworkImage(
                        url = fullPosterUrl,
                        contentDescription = mediaCard.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        errorPlaceholder = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        },
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Movie,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = mediaCard.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                ) {
                    if (!mediaCard.releaseYear.isNullOrBlank()) {
                        Text(
                            text = mediaCard.releaseYear.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (mediaCard.rating > 0.0f) {
                        if (!mediaCard.releaseYear.isNullOrBlank()) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", mediaCard.rating),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (!mediaCard.overview.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = mediaCard.overview.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
