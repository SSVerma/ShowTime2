package com.ssverma.feature.movie.ui.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.filter.ui.filter.FiltersScreen
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig

@Composable
fun MovieFiltersScreen(
    onFilterApplied: (discoverConfig: MovieDiscoverConfig) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    initialConfig: MovieDiscoverConfig? = null,
) {
    FiltersScreen(
        isTv = false,
        modifier = modifier,
        listState = listState,
        initialConfig = initialConfig,
        onBackPressed = onBackPressed,
        onFilterApplied = { filterState ->
            val builder = MovieDiscoverConfig.builder()

            // Apply Sort (SortBy inherently knows its Order)
            filterState.sortBy?.let { builder.sortBy(it) }

            // Safely cast generic options to Movie Options
            val movieOptions = filterState.options
                .filterIsInstance<DiscoverOption.OptionScope.Movie>()
                .toTypedArray()

            // Build and send!
            val config = builder.with(*movieOptions).build()
            onFilterApplied(config)
        }
    )
}
