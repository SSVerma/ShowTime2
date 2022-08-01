package com.ssverma.feature.tv.ui

import MediaItem
import ScoreIndicator
import ValueIndicator
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.layout.SectionLoadingIndicator
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.HomePageAppBar

@Composable
fun TvShowScreen(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
) {
    TvShowContent(
        viewModel = viewModel,
        openTvShowList = openTvShowList,
        openTvShowDetails = openTvShowDetails,
        onSearchIconPressed = openSearchPage,
        onAccountIconPressed = openAccountPage
    )
}

@Composable
private fun TvShowContent(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    onSearchIconPressed: () -> Unit,
    onAccountIconPressed: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            HomePageAppBar(
                backgroundColor = MaterialTheme.colors.background,
                onSearchIconPressed = onSearchIconPressed,
                onAccountIconPressed = onAccountIconPressed
            )
            Spacer(modifier = Modifier.height(DefaultTvShowSectionSpacing))
        }

        //Genre
        item {
            TvShowGenres(
                genreUiState = viewModel.tvGenresUiState,
                onGenreClicked = { genre ->
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = {
                    viewModel.fetchTvGeneres()
                }
            )
        }

        //Trending Today
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.trendingTvShowsUiState,
                sectionTitleRes = R.string.trending_today,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.TrendingToday,
                            titleRes = R.string.trending_today
                        )
                    )
                },
                onRetry = { viewModel.fetchTrendingTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        //Airing Today
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.todayAiringTvShowsUiState,
                sectionTitleRes = R.string.airing_today,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.TodayAiring,
                            titleRes = R.string.airing_today
                        )
                    )
                },
                onRetry = { viewModel.fetchTodayAiringTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = { ValueIndicator(value = it.displayPopularity) },
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        //Popular
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.popularTvShowsUiState,
                sectionTitleRes = R.string.popular,
                subtitleRes = R.string.popular_info,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Popular,
                            titleRes = R.string.popular
                        )
                    )
                },
                onRetry = { viewModel.fetchPopularTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = { ValueIndicator(value = it.displayPopularity) },
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        //Top rated
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.topRatedTvShowsUiState,
                sectionTitleRes = R.string.top_rated,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.TopRated,
                            titleRes = R.string.top_rated
                        )
                    )
                },
                onRetry = { viewModel.fetchTopRatedTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = { ScoreIndicator(score = it.voteAvgPercentage) },
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        //Now airing shows
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.todayAiringTvShowsUiState,
                sectionTitleRes = R.string.now_airing,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.NowAiring,
                            titleRes = R.string.now_airing
                        )
                    )
                },
                onRetry = { viewModel.fetchTodayAiringTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        //Upcoming
        item {
            TvShowsSection(
                tvShowsUiState = viewModel.upcomingTvShowsUiState,
                sectionTitleRes = R.string.upcoming,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Upcoming,
                            titleRes = R.string.upcoming
                        )
                    )
                },
                onRetry = { viewModel.fetchUpcomingTvShows() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = {
                        it.displayFirstAirDate?.let { date ->
                            ValueIndicator(value = date)
                        }
                    },
                    onClick = { openTvShowDetails(it.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(FooterSpacerHeight))
        }
    }
}

@Composable
fun TvShowsSection(
    tvShowsUiState: TvShowListUiState,
    @StringRes sectionTitleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int = 0,
    leadingIconUrl: String? = null,
    onViewAllClicked: () -> Unit = {},
    onRetry: () -> Unit,
    content: @Composable (tvShow: TvShow) -> Unit
) {

    var shouldVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = shouldVisible,
        modifier = modifier.padding(top = 32.dp)
    ) {
        Section(
            sectionHeader = {
                SectionHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    title = stringResource(sectionTitleRes),
                    subtitle = if (subtitleRes == 0) null else stringResource(id = subtitleRes),
                    leadingIconUrl = leadingIconUrl,
                    onTrailingActionClicked = onViewAllClicked
                )
            },
        ) {
            DriveCompose(
                uiState = tvShowsUiState,
                loading = { SectionLoadingIndicator() },
                onRetry = onRetry
            ) { tvShows ->

                shouldVisible = tvShows.isNotEmpty()
                HorizontalLazyList(items = tvShows) { content(it) }
            }
        }
    }
}

@Composable
fun TvShowGenres(
    genreUiState: GenresUiState,
    onRetry: () -> Unit,
    onGenreClicked: (genre: Genre) -> Unit
) {
    DriveCompose(
        uiState = genreUiState,
        loading = { SectionLoadingIndicator() },
        onRetry = onRetry
    ) { genres ->
        HorizontalLazyList(genres) {
            GenreItem(
                genre = it,
                onGenreClicked = { onGenreClicked(it) }
            )
        }
    }
}

private val DefaultTvShowSectionSpacing = 32.dp
private val FooterSpacerHeight = 56.dp
