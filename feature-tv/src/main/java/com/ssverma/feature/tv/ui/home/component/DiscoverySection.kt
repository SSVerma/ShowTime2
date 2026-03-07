package com.ssverma.feature.tv.ui.home.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.MediaItemShimmer
import com.ssverma.shared.ui.component.media.TvShowGridItem

private typealias TvPreviewUiState = UiState<List<TvShowPreview>, TvShowFailure>

data class DiscoveryCategory(
    @StringRes val titleRes: Int,
    val type: Int,
    val uiState: TvPreviewUiState
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverySection(
    popularTvShowsState: TvPreviewUiState,
    topRatedTvShowsState: TvPreviewUiState,
    upcomingTvShowsState: TvPreviewUiState,
    onFetchPopular: () -> Unit,
    onFetchTopRated: () -> Unit,
    onFetchUpcoming: () -> Unit,
    onTvShowClicked: (Int) -> Unit,
    onSeeAllClicked: (TvShowListingArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val categories = remember(popularTvShowsState, topRatedTvShowsState, upcomingTvShowsState) {
        listOf(
            DiscoveryCategory(
                titleRes = R.string.popular,
                type = TvShowListingAvailableTypes.Popular,
                uiState = popularTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.top_rated,
                type = TvShowListingAvailableTypes.TopRated,
                uiState = topRatedTvShowsState
            ),
            DiscoveryCategory(
                titleRes = R.string.upcoming,
                type = TvShowListingAvailableTypes.Upcoming,
                uiState = upcomingTvShowsState
            )
        )
    }

    LaunchedEffect(selectedTabIndex) {
        when (categories[selectedTabIndex].type) {
            TvShowListingAvailableTypes.Popular -> onFetchPopular()
            TvShowListingAvailableTypes.TopRated -> onFetchTopRated()
            TvShowListingAvailableTypes.Upcoming -> onFetchUpcoming()
        }
    }

    Column(modifier = modifier) {
        SectionHeader(
            modifier = Modifier.padding(start = MaterialTheme.spacing.large),
            title = stringResource(R.string.discover),
            onTrailingActionClicked = {
                val category = categories[selectedTabIndex]
                onSeeAllClicked(
                    TvShowListingArgs(
                        listingType = category.type,
                        titleRes = category.titleRes
                    )
                )
            }
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.padding(vertical = MaterialTheme.spacing.smallMedium)
        ) {
            itemsIndexed(categories) { index, category ->
                FilterChip(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    label = {
                        Text(
                            text = stringResource(category.titleRes),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        AnimatedContent(
            targetState = selectedTabIndex,
            label = "DiscoveryContentTransition"
        ) { index ->
            val category = categories[index]

            DriveCompose(
                uiState = category.uiState,
                loading = { DiscoveryLoadingPlaceholder() },
                onRetry = {
                    when (category.type) {
                        TvShowListingAvailableTypes.Popular -> onFetchPopular()
                        TvShowListingAvailableTypes.TopRated -> onFetchTopRated()
                        TvShowListingAvailableTypes.Upcoming -> onFetchUpcoming()
                    }
                }
            ) { tvShows ->
                HorizontalLazyList(
                    items = tvShows,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) { tvShowPreview ->
                    TvShowGridItem(
                        tvShow = tvShowPreview,
                        indicator = { preview ->
                            TvIndicator(type = category.type, tvShow = preview)
                        },
                        onClick = { preview -> onTvShowClicked(preview.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoveryLoadingPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        repeat(3) { MediaItemShimmer() }
    }
}
