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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.padding
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant

@Composable
fun TvShowGridItem(
    tvShow: TvShowPreview,
    modifier: Modifier = Modifier,
    posterModifier: Modifier = Modifier,
    indicator: (@Composable (TvShowPreview) -> Unit)? = null,
    onClick: (TvShowPreview) -> Unit,
) {
    Column(modifier = modifier) {
        MediaPoster(
            posterImageUrl = tvShow.posterImageUrl,
            indicator = indicator?.let { { it(tvShow) } },
            onClick = { onClick(tvShow) },
            overlayContent = {
                WatchProviderTrigger(
                    mediaId = tvShow.id,
                    isMovie = false,
                    variant = WatchProviderTriggerVariant.Icon,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )
            },
            modifier = posterModifier
                .width(MediaItemDefaults.PosterWidth)
                .aspectRatio(TmdbPosterAspectRatio)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = tvShow.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(MediaItemDefaults.PosterWidth)
        )

        if (tvShow.displayYear.isNotEmpty()) {
            Text(
                text = tvShow.displayYear,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(MediaItemDefaults.PosterWidth)
            )
        }
    }
}
