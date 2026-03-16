package com.ssverma.feature.filter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.filter.ui.hub.WatchProviderHubScreen
import com.ssverma.feature.filter.ui.hub.WatchProviderHubSeeAllType
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.movie.navigation.MovieListDestination
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.feature.tv.navigation.TvShowListDestination
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
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
                navController.navigateTo(
                    MovieListDestination.actualRoute(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Discovery,
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            } else {
                navController.navigateTo(
                    TvShowListDestination.actualRoute(
                        TvShowListingArgs(
                            listingType = TvShowListingAvailableTypes.Discovery,
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            }
        },
        onSeeAllClick = { type: WatchProviderHubSeeAllType, isMovie: Boolean, providerId: Int, providerName: String ->
            if (isMovie) {
                val listingType = when (type) {
                    WatchProviderHubSeeAllType.NewThisWeek -> MovieListingAvailableTypes.WatchProviderNew
                    WatchProviderHubSeeAllType.Upcoming -> MovieListingAvailableTypes.WatchProviderUpcoming
                    WatchProviderHubSeeAllType.TopRated -> MovieListingAvailableTypes.WatchProviderTopRated
                }

                navController.navigateTo(
                    MovieListDestination.actualRoute(
                        MovieListingArgs(
                            listingType = listingType,
                            watchProviderId = providerId,
                            title = providerName
                        )
                    )
                )
            } else {
                val listingType = when (type) {
                    WatchProviderHubSeeAllType.NewThisWeek -> TvShowListingAvailableTypes.WatchProviderNew
                    WatchProviderHubSeeAllType.Upcoming -> TvShowListingAvailableTypes.WatchProviderUpcoming
                    WatchProviderHubSeeAllType.TopRated -> TvShowListingAvailableTypes.WatchProviderTopRated
                }

                navController.navigateTo(
                    TvShowListDestination.actualRoute(
                        TvShowListingArgs(
                            listingType = listingType,
                            watchProviderId = providerId,
                            title = providerName
                        )
                    )
                )
            }
        }
    )
}
