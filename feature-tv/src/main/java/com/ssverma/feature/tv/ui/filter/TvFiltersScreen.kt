package com.ssverma.feature.tv.ui.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.filter.ui.filter.FiltersScreen
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TvDiscoverConfig

@Composable
fun TvFiltersScreen(
    onFilterApplied: (filterConfig: TvDiscoverConfig) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    initialConfig: TvDiscoverConfig? = null,
) {
    FiltersScreen(
        isTv = true,
        modifier = modifier,
        listState = listState,
        initialConfig = initialConfig,
        onBackPressed = onBackPressed,
        onFilterApplied = { filterState ->
            val builder = TvDiscoverConfig.builder()

            // Apply Sort (SortBy inherently knows its Order)
            filterState.sortBy?.let { builder.sortBy(it) }

            // Safely cast generic options to Tv Options
            val tvOptions = filterState.options
                .filterIsInstance<DiscoverOption.OptionScope.Tv>()
                .toTypedArray()

            // Build and send!
            val config = builder.with(*tvOptions).build()
            onFilterApplied(config)
        }
    )
}
