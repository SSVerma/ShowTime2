package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun MovieGridItem(
    movie: MoviePreview,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    indicator: (@Composable (MoviePreview) -> Unit)? = null,
    onClick: (MoviePreview) -> Unit
) {
    Column(modifier = modifier) {
        MediaPoster(
            posterImageUrl = movie.posterImageUrl,
            indicator = indicator?.let { { it(movie) } },
            onClick = { onClick(movie) },
            modifier = posterModifier
                .width(MediaItemDefaults.PosterWidth)
                .aspectRatio(TmdbPosterAspectRatio)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = movie.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
        )

        if (movie.displayYear.isNotEmpty()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
            Text(
                text = movie.displayYear,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
