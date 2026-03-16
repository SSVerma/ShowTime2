package com.ssverma.feature.tv.ui.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.filter.ui.filter.FiltersScreen
import com.ssverma.shared.domain.TvDiscoverConfig

@Composable
fun TvFiltersScreen(
    onFilterApplied: (filterConfig: TvDiscoverConfig) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    watchRegion: String? = null,
    listState: LazyListState = rememberLazyListState(),
    initialConfig: TvDiscoverConfig? = null,
) {
    FiltersScreen(
        isTv = true,
        modifier = modifier,
        listState = listState,
        watchRegion = watchRegion,
        initialOptions = initialConfig?.discoverOptions?.toList() ?: emptyList(),
        initialSortBy = initialConfig?.sortBy,
        initialOrder = initialConfig?.sortBy?.order,
        onBackPressed = onBackPressed,
        onFilterApplied = { appliedOptions, sortBy, order ->
            val builder = TvDiscoverConfig.builder()
            val finalSortBy = if (sortBy != null && order != null) {
                sortBy.withOrder(order)
            } else sortBy

            finalSortBy?.let { builder.sortBy(it) }
            val config = builder
                .with(*appliedOptions.toTypedArray())
                .build()
            onFilterApplied(config)
        }
    )
}
