package com.ssverma.feature.tv.ui.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.filter.ui.filter.FilterGroup
import com.ssverma.feature.filter.ui.filter.FiltersScreen
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TvDiscoverConfig

@Composable
fun TvFiltersScreen(
    filterGroups: List<FilterGroup>,
    onFilterApplied: (discoverConfig: TvDiscoverConfig) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
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
