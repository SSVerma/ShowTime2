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
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.analytics.MovieAnalyticsEvent
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.analytics.MovieAnalyticsValues
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.movie.ui.home.HomeMovieViewModel
import com.ssverma.feature.movie.ui.home.component.DiscoverySection
import com.ssverma.feature.movie.ui.home.component.HeroSection
import com.ssverma.feature.movie.ui.home.component.MovieGenres
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.shared.ui.component.media.MovieListItem

@Composable
fun MovieHomeContent(
    viewModel: HomeMovieViewModel,
    openMovieList: (MovieListingArgs) -> Unit,
    openMovieDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            HeroSection(
                trendingMoviesState = uiState.trendingMovies,
                onSearchClicked = openSearchPage,
                onAccountClicked = openAccountPage,
                onMovieClicked = { movie ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movie = movie,
                            section = MovieAnalyticsValues.SECTION_TRENDING_CAROUSEL,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieDetails(movie.id)
                },
                onRetry = { viewModel.fetchTrendingMovies() }
            )
        }

        item {
            MovieGenres(
                genresUiState = uiState.genres,
                onGenreClicked = { genre ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                },
                onRetry = { viewModel.fetchMovieGenres() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.large)
            )
        }

        item {
            WatchProviderHubSection(
                providersUiState = uiState.watchProviders,
                onProviderClick = { provider ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.WatchProviderClicked(
                            providerInfo = provider,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME
                        )
                    )
                    openWatchProviderHub(provider)
                },
                onRetry = { viewModel.fetchWatchProviders() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            DiscoverySection(
                popularMoviesState = uiState.popularMovies,
                topRatedMoviesState = uiState.topRatedMovies,
                upcomingMoviesState = uiState.upcomingMovies,
                onMovieClicked = { moviePreview: MoviePreview ->
                    analytics.logEvent(
                        MovieAnalyticsEvent.MovieClicked(
                            movie = moviePreview,
                            section = MovieAnalyticsValues.SECTION_DISCOVERY,
                            sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                        )
                    )
                    openMovieDetails(moviePreview.id)
                },
                onSeeAllClicked = { args ->
                    analytics.logEvent(MovieAnalyticsEvent.SeeAllClicked(section = MovieAnalyticsValues.SECTION_DISCOVERY))
                    openMovieList(args)
                },
                onFetchPopular = { viewModel.fetchPopularMovies() },
                onFetchTopRated = { viewModel.fetchTopRatedMovies() },
                onFetchUpcoming = { viewModel.fetchUpcomingMovies() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
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
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
            ) { moviePreview ->
                MovieListItem(
                    movie = moviePreview,
                    showRating = true,
                    onClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.MovieClicked(
                                movie = it,
                                section = MovieAnalyticsValues.SECTION_IN_CINEMAS,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_HOME,
                            )
                        )
                        openMovieDetails(it.id)
                    },
                    indicator = {
                        MovieIndicator(
                            type = MovieListingAvailableTypes.NowInCinemas,
                            movie = it
                        )
                    },
                )
            }
        }


        item { AttributionFooter(modifier = Modifier.padding(top = MaterialTheme.spacing.large)) }
    }
}
