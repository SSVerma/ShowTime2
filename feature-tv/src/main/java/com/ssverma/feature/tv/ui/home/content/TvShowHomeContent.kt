package com.ssverma.feature.tv.ui.home.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.ui.home.HomeTvShowViewModel
import com.ssverma.feature.tv.ui.home.component.DiscoverySection
import com.ssverma.feature.tv.ui.home.component.HeroSection
import com.ssverma.feature.tv.ui.home.component.TvGenres
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.media.TvShowListItem
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.core.ui.UiState

@Composable
fun TvShowHomeContent(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (TvShowListingArgs) -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            HeroSection(
                trendingTvShowsState = uiState.trendingTvShows,
                onSearchClicked = openSearchPage,
                onAccountClicked = openAccountPage,
                onTvShowClicked = openTvShowDetails,
                onRetry = { viewModel.fetchTrendingTvShows() }
            )
        }

        item {
            TvGenres(
                genresUiState = uiState.genres,
                onGenreClicked = { genre ->
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = { viewModel.fetchTvGenres() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.large)
            )
        }

        item {
            DiscoverySection(
                popularTvShowsState = uiState.popularTvShows,
                topRatedTvShowsState = uiState.topRatedTvShows,
                upcomingTvShowsState = uiState.upcomingTvShows,
                onTvShowClicked = openTvShowDetails,
                onSeeAllClicked = openTvShowList,
                onFetchPopular = { viewModel.fetchPopularTvShows() },
                onFetchTopRated = { viewModel.fetchTopRatedTvShows() },
                onFetchUpcoming = { viewModel.fetchUpcomingTvShows() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            AppSection(
                title = stringResource(R.string.airing_today),
                uiState = uiState.todayAiringTvShows,
                isVertical = true,
                onTrailingActionClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.TodayAiring,
                            titleRes = R.string.airing_today
                        )
                    )
                },
                onRetry = { viewModel.fetchTodayAiringTvShows() },
                loadingPlaceholder = { MediaListItemShimmer() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
            ) { tvShowPreview ->
                TvShowListItem(
                    tvShow = tvShowPreview,
                    showRating = true,
                    onClick = { openTvShowDetails(it.id) },
                    indicator = {
                        TvIndicator(
                            type = TvShowListingAvailableTypes.TodayAiring,
                            tvShow = it
                        )
                    },
                )
            }
        }

        item {
            AppSection(
                title = stringResource(R.string.now_airing),
                uiState = uiState.nowAiringTvShows,
                isVertical = true,
                onTrailingActionClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.NowAiring,
                            titleRes = R.string.now_airing
                        )
                    )
                },
                onRetry = { viewModel.fetchNowAiringTvShows() },
                loadingPlaceholder = { MediaListItemShimmer() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
            ) { tvShowPreview ->
                TvShowListItem(
                    tvShow = tvShowPreview,
                    showRating = true,
                    onClick = { openTvShowDetails(it.id) },
                    indicator = {
                        TvIndicator(
                            type = TvShowListingAvailableTypes.NowAiring,
                            tvShow = it
                        )
                    },
                )
            }
        }


        item { AttributionFooter(modifier = Modifier.padding(top = MaterialTheme.spacing.large)) }
    }
}
