package com.ssverma.feature.tv.ui.list.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant
import com.ssverma.shared.ui.component.media.TvShowGridItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvShowsGridContent(
    tvShowPagingItems: LazyPagingItems<TvShowPreview>,
    type: Int,
    openTvShowDetails: (TvShowPreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    PagedGrid(
        pagingItems = tvShowPagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { tvShow ->
        TvShowGridItem(
            tvShow = tvShow,
            showRating = type != TvShowListingAvailableTypes.Upcoming && type != TvShowListingAvailableTypes.TopRated,
            indicator = { preview -> TvIndicator(type = type, tvShow = preview) },
            onClick = openTvShowDetails,
            overlayContent = {
                WatchProviderTrigger(
                    mediaId = it.id,
                    isMovie = false,
                    variant = WatchProviderTriggerVariant.Icon,
                    onWatchProviderClick = onWatchProviderClick,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )
            },
            posterModifier = Modifier.fillMaxWidth()
        )
    }
}
