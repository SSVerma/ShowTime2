package com.ssverma.feature.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.model.TvSeason
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@OptIn(ExperimentalMaterialApi::class)
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
                Text(text = tvSeason.title, style = MaterialTheme.typography.subtitle1)
                Text(
                    text = tvSeason.displayAirDate.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = stringResource(id = R.string.episodes_n, tvSeason.episodeCount),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = tvSeason.overview,
                    style = MaterialTheme.typography.caption,
                    maxLines = 2,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}