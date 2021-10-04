package com.ssverma.showtime.ui.tv

import MediaItem
import ScoreIndicator
import ValueIndicator
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.insets.statusBarsHeight
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.home.HomePageAppBar
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.movie.GenreItem

enum class TvShowListingType {
    TrendingToday,
    AiringToday,
    Popular,
    TopRated,
    NowAiring,
    Upcoming,
    Genre
}

data class TvShowListLaunchable(
    val listingType: TvShowListingType,
    @StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genre: Genre? = null
)

@Composable
fun TvShowScreen(
    viewModel: HomeViewModel,
    openTvShowList: (tvShowLaunchable: TvShowListLaunchable) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit
) {
    TvShowContent(
        viewModel = viewModel,
        openTvShowList = openTvShowList,
        openTvShowDetails = openTvShowDetails
    )
}

@Composable
private fun TvShowContent(
    viewModel: HomeViewModel,
    openTvShowList: (tvShowLaunchable: TvShowListLaunchable) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Spacer(modifier = Modifier.statusBarsHeight())
            HomePageAppBar(backgroundColor = MaterialTheme.colors.background)
            Spacer(modifier = Modifier.height(DefaultTvShowSectionSpacing))
        }

        //Genre
        item {
            TvShowGenres(
                liveGenres = viewModel.tvGenres,
                onGenreClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.Genre,
                            title = it.name,
                            genre = it
                        )
                    )
                }
            )
        }

        //Trending Today
        item {
            TvShowsSection(
                liveTvShows = viewModel.dailyTrendingTvShows,
                sectionTitleRes = R.string.trending_today,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.TrendingToday,
                            titleRes = R.string.trending_today
                        )
                    )
                }
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
                liveTvShows = viewModel.todayAiringTvShows,
                sectionTitleRes = R.string.airing_today,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.AiringToday,
                            titleRes = R.string.airing_today
                        )
                    )
                }
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
                liveTvShows = viewModel.popularTvShows,
                sectionTitleRes = R.string.popuplar,
                subtitleRes = R.string.popular_info,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.Popular,
                            titleRes = R.string.popuplar
                        )
                    )
                }
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
                liveTvShows = viewModel.topRatedTvShows,
                sectionTitleRes = R.string.top_rated,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.TopRated,
                            titleRes = R.string.top_rated
                        )
                    )
                }
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
                liveTvShows = viewModel.nowAiringTvShows,
                sectionTitleRes = R.string.now_airing,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.NowAiring,
                            titleRes = R.string.now_airing
                        )
                    )
                }
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
                liveTvShows = viewModel.upcomingTvShows,
                sectionTitleRes = R.string.upcoming,
                onViewAllClicked = {
                    openTvShowList(
                        TvShowListLaunchable(
                            listingType = TvShowListingType.Upcoming,
                            titleRes = R.string.upcoming
                        )
                    )
                }
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TvShowsSection(
    liveTvShows: LiveData<Result<List<TvShow>>>,
    @StringRes sectionTitleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int = 0,
    leadingIconUrl: String? = null,
    onViewAllClicked: () -> Unit = {},
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
                observable = liveTvShows,
                loading = { SectionLoadingIndicator() }
            ) { tvShows ->

                shouldVisible = tvShows.isNotEmpty()
                HorizontalList(items = tvShows) { content(it) }
            }
        }
    }
}

@Composable
fun TvShowGenres(liveGenres: LiveData<Result<List<Genre>>>, onGenreClicked: (genre: Genre) -> Unit) {
    DriveCompose(
        observable = liveGenres,
        loading = { SectionLoadingIndicator() }
    ) { genres ->
        HorizontalList(genres) {
            GenreItem(
                genre = it,
                onGenreClicked = { onGenreClicked(it) }
            )
        }
    }
}

private val DefaultTvShowSectionSpacing = 32.dp
private val FooterSpacerHeight = 56.dp
