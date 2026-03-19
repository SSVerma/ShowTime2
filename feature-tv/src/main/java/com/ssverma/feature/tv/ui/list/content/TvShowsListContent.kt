package com.ssverma.feature.tv.ui.list.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.media.TvShowListItem

@Composable
fun TvShowsListContent(
    tvShowPagingItems: LazyPagingItems<TvShowPreview>,
    config: TvShowListingConfig,
    openTvShowDetails: (TvShowPreview) -> Unit,
    onWatchProviderClick: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    PagedList(
        pagingItems = tvShowPagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { tvShow ->
        TvShowListItem(
            tvShow = tvShow,
            showRating = config !is TvShowListingConfig.Filterable.Upcoming && config !is TvShowListingConfig.Filterable.TopRated,
            indicator = { preview -> TvIndicator(config = config, tvShow = preview) },
            onWatchProviderClick = onWatchProviderClick,
            onClick = openTvShowDetails,
        )
    }
}
