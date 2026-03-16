package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant

@Composable
fun MovieGridItem(
    movie: MoviePreview,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    showRating: Boolean = true,
    indicator: (@Composable (MoviePreview) -> Unit)? = null,
    overlayContent: (@Composable (MoviePreview) -> Unit)? = {
        WatchProviderTrigger(
            mediaId = it.id,
            isMovie = true,
            variant = WatchProviderTriggerVariant.Icon,
            modifier = Modifier.padding(MaterialTheme.spacing.small)
        )
    },
    onClick: (MoviePreview) -> Unit,
) {
    Column(modifier = modifier) {
        MediaPoster(
            posterImageUrl = movie.posterImageUrl,
            indicator = indicator?.let { { it(movie) } },
            onClick = { onClick(movie) },
            overlayContent = {
                overlayContent?.invoke(movie)
            },
            modifier = posterModifier
                .width(MediaItemDefaults.PosterWidth)
                .aspectRatio(TmdbPosterAspectRatio)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = movie.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
        ) {
            if (showRating && movie.voteCount > 0) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = FormatterUtils.formatRating(movie.voteAvgPercentage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (movie.displayYear.isNotEmpty()) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (movie.displayYear.isNotEmpty()) {
                Text(
                    text = movie.displayYear,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
