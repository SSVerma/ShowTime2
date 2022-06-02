package com.ssverma.feature.movie.ui.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.core.domain.DiscoverOption
import com.ssverma.core.domain.MovieDiscoverConfig
import com.ssverma.feature.filter.ui.FilterGroup
import com.ssverma.feature.filter.ui.FiltersScreen

@Composable
fun MovieFiltersScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filterGroups: List<FilterGroup>,
    onFilterApplied: (discoverConfig: MovieDiscoverConfig) -> Unit
) {
    FiltersScreen<DiscoverOption.OptionScope.Movie>(
        modifier = modifier,
        filterGroups = filterGroups,
        listState = listState,
        onFilterApplied = { options ->
            val config = MovieDiscoverConfig.builder()
                .with(*options.toTypedArray())
                .build()
            onFilterApplied(config)
        }
    )
}