package com.ssverma.feature.tv.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.feature.filter.ui.FilterGroup
import com.ssverma.feature.filter.ui.FiltersScreen

@Composable
fun TvFiltersScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filterGroups: List<FilterGroup>,
    onFilterApplied: (discoverConfig: TvDiscoverConfig) -> Unit
) {
    FiltersScreen<DiscoverOption.OptionScope.Tv>(
        modifier = modifier,
        filterGroups = filterGroups,
        listState = listState,
        onFilterApplied = { options ->
            val config = TvDiscoverConfig.builder()
                .with(*options.toTypedArray())
                .build()
            onFilterApplied(config)
        }
    )
}