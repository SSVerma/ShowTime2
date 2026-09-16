package com.ssverma.feature.tv.ui.details.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.ui.details.TvSeasonItem
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing

fun LazyListScope.tvSeasonsSection(
    seasons: List<TvSeason>,
    seasonWatchCounts: Map<Int, Int>,
    onSeasonClick: (TvSeason) -> Unit,
    onToggleSeasonWatched: (TvSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    if (seasons.isNotEmpty()) {
        item(key = "tv_seasons", contentType = "seasons") {
            SeasonsSection(
                seasons = seasons,
                seasonWatchCounts = seasonWatchCounts,
                onSeasonClick = onSeasonClick,
                onToggleSeasonWatched = onToggleSeasonWatched,
                modifier = modifier
            )
        }
    }
}

@Composable
fun SeasonsSection(
    seasons: List<TvSeason>,
    seasonWatchCounts: Map<Int, Int>,
    onSeasonClick: (season: TvSeason) -> Unit,
    onToggleSeasonWatched: (season: TvSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    var seasonCount by rememberSaveable {
        mutableIntStateOf(if (seasons.size < 3) seasons.size else 3)
    }

    val showSeasonViewAll by remember { derivedStateOf { seasonCount < seasons.size } }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.seasons_n, seasons.size),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = seasons.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
        ) {
            for (i in 0 until seasonCount) {
                val season = seasons[i]
                val watchedCount = seasonWatchCounts[season.seasonNumber] ?: 0
                TvSeasonItem(
                    tvSeason = season,
                    watchedEpisodeCount = watchedCount,
                    onClick = {
                        onSeasonClick(season)
                    },
                    onToggleWatched = {
                        onToggleSeasonWatched(season)
                    }
                )
            }
            if (showSeasonViewAll) {
                OutlinedButton(
                    onClick = { seasonCount = seasons.size },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.view_more),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
