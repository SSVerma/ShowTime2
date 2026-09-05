package com.ssverma.feature.tv.ui.list.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvShowsGridContent(
    tvShowPagingItems: LazyPagingItems<TvShowPreview>,
    config: TvShowListingConfig,
    openTvShowDetails: (TvShowPreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null
) {
    PagedGrid(
        pagingItems = tvShowPagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { tvShow ->
        UniversalMediaCard(
            item = tvShow.asUniversalMediaItem(),
            onClick = { openTvShowDetails(tvShow) },
            isGridView = true,
            topStartSlot = {
                val hasIndicator = when (config) {
                    is TvShowListingConfig.Filterable.Popular,
                    is TvShowListingConfig.Filterable.TopRated,
                    is TvShowListingConfig.Filterable.Upcoming -> true

                    else -> false
                }
                if (hasIndicator) {
                    TvIndicator(config = config, tvShow = tvShow)
                } else if (tvShow.voteAvg > 0f) {
                    MediaCardRatingBadge(rating = tvShow.voteAvg)
                }
            },
            onShowFeedback = onShowFeedback,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
