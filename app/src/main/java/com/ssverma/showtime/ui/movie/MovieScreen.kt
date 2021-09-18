package com.ssverma.showtime.ui.movie

import MovieItem
import ScoreIndicator
import ValueIndicator
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.insets.statusBarsHeight
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.home.HomePageAppBar
import com.ssverma.showtime.ui.home.HomeViewModel

@Composable
fun MovieScreen(
    viewModel: HomeViewModel,
    openMovieList: (launchable: MovieListLaunchable) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    MovieContent(
        viewModel = viewModel,
        openMovieList = openMovieList,
        openMovieDetails = openMovieDetails
    )
}

@Composable
private fun MovieContent(
    viewModel: HomeViewModel,
    openMovieList: (launchable: MovieListLaunchable) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .animateContentSize()
    ) {

        HeaderSection(
            viewModel = viewModel,
            onNavigateToMovieList = openMovieList,
            openMovieDetails = openMovieDetails
        )

        //Popular
        MoviesSection(
            liveMovies = viewModel.popularMovies,
            sectionTitleRes = R.string.popuplar,
            subtitleRes = R.string.popular_info,
            onViewAllClicked = {
                openMovieList(
                    MovieListLaunchable(
                        listingType = MovieListingType.Popular,
                        titleRes = R.string.popuplar
                    )
                )
            }
        ) {
            MovieItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                indicator = { ValueIndicator(value = it.displayPopularity) },
                onClick = { openMovieDetails(it.id) }
            )
        }

        //Top rated
        MoviesSection(
            liveMovies = viewModel.topRatedMovies,
            sectionTitleRes = R.string.top_rated,
            onViewAllClicked = {
                openMovieList(
                    MovieListLaunchable(
                        listingType = MovieListingType.TopRated,
                        titleRes = R.string.top_rated
                    )
                )
            }
        ) {
            MovieItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                indicator = { ScoreIndicator(score = it.voteAvgPercentage) },
                onClick = { openMovieDetails(it.id) }
            )
        }

        //Released
        MoviesSection(
            liveMovies = viewModel.nowInCinemasMovies,
            sectionTitleRes = R.string.now_in_cinemas,
            onViewAllClicked = {
                openMovieList(
                    MovieListLaunchable(
                        listingType = MovieListingType.NowInCinemas,
                        titleRes = R.string.now_in_cinemas
                    )
                )
            }
        ) {
            MovieItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                onClick = { openMovieDetails(it.id) }
            )
        }

        //Upcoming
        MoviesSection(
            liveMovies = viewModel.upcomingMovies,
            sectionTitleRes = R.string.upcoming,
            onViewAllClicked = {
                openMovieList(
                    MovieListLaunchable(
                        listingType = MovieListingType.Upcoming,
                        titleRes = R.string.upcoming
                    )
                )
            }
        ) {
            MovieItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                indicator = {
                    it.displayReleaseDate?.let { date ->
                        ValueIndicator(value = date)
                    }
                },
                onClick = { openMovieDetails(it.id) }
            )
        }
        Spacer(modifier = Modifier.height(FooterSpacerHeight))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoviesSection(
    liveMovies: LiveData<Result<List<Movie>>>,
    @StringRes sectionTitleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int = 0,
    leadingIconUrl: String? = null,
    onViewAllClicked: () -> Unit = {},
    content: @Composable (movie: Movie) -> Unit
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
                observable = liveMovies,
                loading = { SectionLoadingIndicator() }
            ) { movies ->

                shouldVisible = movies.isNotEmpty()
                HorizontalList(items = movies) { content(it) }
            }
        }
    }
}

@Composable
fun HeaderSection(
    viewModel: HomeViewModel,
    onNavigateToMovieList: (launchable: MovieListLaunchable) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    val blurColor = MaterialTheme.colors.background
    val scrimColor = MaterialTheme.colors.onBackground

    Box(modifier = Modifier.background(color = scrimColor)) {
        NetworkImage(
            url = viewModel.movieBackdrop,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .aspectRatio(1f)
                .scrim(
                    colors = listOf(
                        scrimColor.copy(alpha = 0.20f),
                        scrimColor.copy(alpha = 0.50f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            blurColor.copy(alpha = 0.0f),
                            blurColor.copy(alpha = 0.33f),
                            blurColor.copy(alpha = 0.85f),
                            blurColor.copy(alpha = 1f),
                            blurColor.copy(alpha = 1f)
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.statusBarsHeight())
            HomePageAppBar(
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                contentColor = Color.White
            )
            Spacer(modifier = Modifier.height(DefaultMovieSectionSpacing))
            MovieGenres(
                liveGenres = viewModel.genres,
                onGenreClicked = {
                    onNavigateToMovieList(
                        MovieListLaunchable(
                            listingType = MovieListingType.Genre,
                            title = it.name,
                            genre = it
                        )
                    )
                }
            )
            MoviesSection(
                liveMovies = viewModel.dailyTrendingMovies,
                sectionTitleRes = R.string.trending_today,
                onViewAllClicked = {
                    onNavigateToMovieList(
                        MovieListLaunchable(
                            listingType = MovieListingType.TrendingToday,
                            titleRes = R.string.trending_today
                        )
                    )
                }
            ) {
                MovieItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    onClick = { openMovieDetails(it.id) }
                )
            }
        }
    }
}

@Composable
fun MovieGenres(liveGenres: LiveData<Result<List<Genre>>>, onGenreClicked: (genre: Genre) -> Unit) {
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

private val DefaultMovieSectionSpacing = 32.dp
private val FooterSpacerHeight = 56.dp
