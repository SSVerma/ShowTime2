package com.ssverma.feature.movie.ui.home.content

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
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.ui.home.HomeMovieViewModel
import com.ssverma.feature.movie.ui.home.component.DiscoverySection
import com.ssverma.feature.movie.ui.home.component.HeroSection
import com.ssverma.feature.movie.ui.home.component.MovieGenres
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.media.MovieListItem

@Composable
fun MovieHomeContent(
    viewModel: HomeMovieViewModel,
    openMovieList: (MovieListingArgs) -> Unit,
    openMovieDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            HeroSection(
                trendingMoviesState = uiState.trendingMovies,
                onSearchClicked = openSearchPage,
                onAccountClicked = openAccountPage,
                onMovieClicked = openMovieDetails,
                onRetry = { viewModel.fetchTrendingMovies() }
            )
        }

        item {
            MovieGenres(
                genresUiState = uiState.genres,
                onGenreClicked = { genre ->
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = { viewModel.fetchMovieGenres() }
            )
        }

        item {
            DiscoverySection(
                popularMoviesState = uiState.popularMovies,
                topRatedMoviesState = uiState.topRatedMovies,
                upcomingMoviesState = uiState.upcomingMovies,
                onMovieClicked = openMovieDetails,
                onSeeAllClicked = openMovieList,
                onRetryPopular = { viewModel.fetchPopularMovies() },
                onRetryTopRated = { viewModel.fetchTopRatedMovies() },
                onRetryUpcoming = { viewModel.fetchUpcomingMovies() }
            )
        }

        item {
            AppSection(
                title = stringResource(R.string.now_in_cinemas),
                uiState = uiState.inCinemasMovies,
                isVertical = true,
                onTrailingActionClicked = {
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.NowInCinemas,
                            titleRes = R.string.now_in_cinemas
                        )
                    )
                },
                onRetry = { viewModel.fetchInCinemaMovies() },
                loadingPlaceholder = { MediaListItemShimmer() },
            ) { moviePreview ->
                MovieListItem(
                    movie = moviePreview,
                    onClick = { openMovieDetails(it.id) },
                    indicator = {
                        MovieIndicator(
                            type = MovieListingAvailableTypes.NowInCinemas,
                            movie = it
                        )
                    }
                )
            }
        }

        item { AttributionFooter(modifier = Modifier.padding(top = MaterialTheme.spacing.large)) }
    }
}
