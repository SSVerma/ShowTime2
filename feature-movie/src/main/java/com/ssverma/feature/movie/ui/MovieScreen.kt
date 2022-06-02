package com.ssverma.feature.movie.ui

import MediaItem
import ScoreIndicator
import ValueIndicator
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.image.NetworkImage
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.layout.SectionLoadingIndicator
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.HomePageAppBar

@Composable
fun MovieScreen(
    viewModel: HomeMovieViewModel,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
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
    viewModel: HomeMovieViewModel,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            HeaderSection(
                viewModel = viewModel,
                onNavigateToMovieList = openMovieList,
                openMovieDetails = openMovieDetails
            )
        }

        //Popular
        item {
            MoviesSection(
                moviesState = viewModel.popularMoviesUiState,
                sectionTitleRes = R.string.popuplar,
                subtitleRes = R.string.popular_info,
                onViewAllClicked = {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Popular,
                            titleRes = R.string.popuplar
                        )
                    )
                },
                onRetry = { viewModel.fetchPopularMovies() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = { ValueIndicator(value = it.displayPopularity) },
                    onClick = { openMovieDetails(it.id) }
                )
            }
        }

        //Top rated
        item {
            MoviesSection(
                moviesState = viewModel.topRatedMoviesUiState,
                sectionTitleRes = R.string.top_rated,
                onViewAllClicked = {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.TopRated,
                            titleRes = R.string.top_rated
                        )
                    )
                },
                onRetry = { viewModel.fetchTopRatedMovies() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    indicator = { ScoreIndicator(score = it.voteAvgPercentage) },
                    onClick = { openMovieDetails(it.id) }
                )
            }
        }

        //Released
        item {
            MoviesSection(
                moviesState = viewModel.inCinemasMoviesUiState,
                sectionTitleRes = R.string.now_in_cinemas,
                onViewAllClicked = {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.NowInCinemas,
                            titleRes = R.string.now_in_cinemas
                        )
                    )
                },
                onRetry = { viewModel.fetchInCinemaMovies() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    onClick = { openMovieDetails(it.id) }
                )
            }
        }

        //Upcoming
        item {
            MoviesSection(
                moviesState = viewModel.upcomingMoviesUiState,
                sectionTitleRes = R.string.upcoming,
                onViewAllClicked = {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Upcoming,
                            titleRes = R.string.upcoming
                        )
                    )
                },
                onRetry = { viewModel.fetchUpcomingMovies() }
            ) {
                MediaItem(
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
        }

        item {
            AttributionFooter(
                modifier = Modifier.padding(top = FooterSpacerHeight)
            )
        }
    }
}

@Composable
fun MoviesSection(
    moviesState: MovieListUiState,
    @StringRes sectionTitleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int = 0,
    leadingIconUrl: String? = null,
    onViewAllClicked: () -> Unit = {},
    onRetry: () -> Unit,
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
                uiState = moviesState,
                loading = { SectionLoadingIndicator() },
                onRetry = onRetry
            ) { movies ->

                shouldVisible = movies.isNotEmpty()
                HorizontalLazyList(items = movies) { content(it) }
            }
        }
    }
}

@Composable
fun HeaderSection(
    viewModel: HomeMovieViewModel,
    onNavigateToMovieList: (listingArgs: MovieListingArgs) -> Unit,
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
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            HomePageAppBar(
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                contentColor = Color.White
            )
            Spacer(modifier = Modifier.height(DefaultMovieSectionSpacing))
            MovieGenres(
                generesUiState = viewModel.movieGenresUiState,
                onGenreClicked = { genre ->
                    onNavigateToMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = { viewModel.fetchMovieGeneres() }
            )
            MoviesSection(
                moviesState = viewModel.trendingMoviesUiState,
                sectionTitleRes = R.string.trending_today,
                onViewAllClicked = {
                    onNavigateToMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.TrendingToday,
                            titleRes = R.string.trending_today
                        )
                    )
                },
                onRetry = { viewModel.fetchTrendingMovies() }
            ) {
                MediaItem(
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    onClick = { openMovieDetails(it.id) }
                )
            }
        }
    }
}

@Composable
fun MovieGenres(
    generesUiState: GenresUiState,
    onRetry: () -> Unit,
    onGenreClicked: (genre: Genre) -> Unit
) {
    DriveCompose(
        uiState = generesUiState,
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

private val DefaultMovieSectionSpacing = 32.dp
private val FooterSpacerHeight = 56.dp
