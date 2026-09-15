package com.ssverma.feature.tv.ui.details.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem

fun LazyListScope.tvSimilarShowsSection(
    tvShows: List<TvShow>,
    onTvShowClick: (TvShow) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tvShows.isNotEmpty()) {
        item(key = "tv_similar_shows", contentType = "similar_shows") {
            SimilarTvShowsSection(
                tvShows = tvShows,
                sectionTitleRes = R.string.similar_shows,
                onTvShowClick = onTvShowClick,
                onShowFeedback = onShowFeedback,
                modifier = modifier
            )
        }
    }
}

fun LazyListScope.tvRecommendationsSection(
    tvShows: List<TvShow>,
    onTvShowClick: (TvShow) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tvShows.isNotEmpty()) {
        item(key = "tv_recommendations", contentType = "recommendations") {
            SimilarTvShowsSection(
                tvShows = tvShows,
                sectionTitleRes = R.string.recommendations,
                onTvShowClick = onTvShowClick,
                onShowFeedback = onShowFeedback,
                modifier = modifier
            )
        }
    }
}

@Composable
fun SimilarTvShowsSection(
    tvShows: List<TvShow>,
    @StringRes sectionTitleRes: Int,
    onTvShowClick: (tvShow: TvShow) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit = {},
    modifier: Modifier = Modifier
) {
    HorizontalLazyListSection(
        items = tvShows,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        itemContent = {
            UniversalMediaCard(
                item = it.asUniversalMediaItem(),
                isGridView = true,
                onShowFeedback = onShowFeedback,
                onClick = { onTvShowClick(it) },
                modifier = Modifier.width(MediaItemDefaults.PosterWidth)
            )
        },
        hideIf = tvShows.isEmpty(),
        modifier = modifier
    )
}
