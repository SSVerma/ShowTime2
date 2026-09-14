package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.R as SharedR
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.TextBadge

@Composable
fun TvSeasonHeroHeader(
    tvSeason: TvSeason,
    tvShowTitle: String?,
    tvShowBackdropPath: String?,
    onBackPress: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avgRating = remember(tvSeason.episodes) {
        val rated = tvSeason.episodes.filter { it.voteAvg > 0f }
        if (rated.isNotEmpty()) {
            rated.map { it.voteAvg }.average().toFloat()
        } else {
            null
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        BackdropHeader(
            backdropImageUrl = tvShowBackdropPath ?: tvSeason.posterImageUrl,
            onCloseIconClick = onBackPress,
            showTrailerFab = false,
            onTrailerFabClick = {},
            secondaryActions = {
                BackdropActionButton(
                    onClick = onShareClick,
                    icon = Icons.Rounded.Share,
                    contentDescription = stringResource(id = SharedR.string.share)
                )
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(top = MaterialTheme.spacing.small)
        ) {
            if (!tvShowTitle.isNullOrBlank()) {
                Emphasize {
                    Text(
                        text = tvShowTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = tvSeason.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                modifier = Modifier.padding(top = MaterialTheme.spacing.small)
            ) {
                avgRating?.let { rating ->
                    ScoreBadge(score = rating * 10)
                }

                tvSeason.displayAirDate?.let { airDate ->
                    DateBadge(dateText = airDate, showYear = true)
                }

                if (tvSeason.episodes.isNotEmpty()) {
                    TextBadge(
                        text = stringResource(id = R.string.episodes_n, tvSeason.episodes.size),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
