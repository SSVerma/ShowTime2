package com.ssverma.feature.filter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.filter.ui.hub.WatchProviderHubScreen
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.shared.domain.model.Genre
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination

fun NavGraphBuilder.watchProviderHubGraph(
    navController: NavController,
) = composable(destination = WatchProviderHubDestination) {
    WatchProviderHubScreen(
        onMovieClick = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        onTvShowClick = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        onBackClick = {
            navController.popBackStack()
        },
        onGenreClick = { genre: Genre, isMovie: Boolean ->
            if (isMovie) {
                navController.navigate(
                    route = MovieListingRoute(
                        MovieListingArgs.ByGenre(
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            } else {
                navController.navigate(
                    route = TvShowListingRoute(
                        args = TvShowListingArgs.ByGenre(
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            }
        },
        onMovieSeeAllClick = { providerInfo, discoverConfig ->
            navController.navigate(
                route = MovieListingRoute(
                    MovieListingArgs.Discovery(
                        initialConfig = discoverConfig,
                        title = providerInfo.providerName,
                    )
                )
            )
        },
        onTvSeeAllClick = { provider, discoverConfig ->
            navController.navigate(
                route = TvShowListingRoute(
                    args = TvShowListingArgs.Discovery(
                        initialConfig = discoverConfig,
                        title = provider.providerName
                    )
                )
            )
        }
    )
}
