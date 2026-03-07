package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun TvSeasonItem(
    tvSeason: TvSeason,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                url = tvSeason.posterImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(88.dp)
                    .aspectRatio(TmdbPosterAspectRatio)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = tvSeason.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = tvSeason.displayAirDate.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(id = R.string.episodes_n, tvSeason.episodeCount),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = tvSeason.overview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
