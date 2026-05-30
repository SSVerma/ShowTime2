package com.ssverma.feature.movie.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.PeerEnterTransition
import com.ssverma.core.navigation.PeerExitTransition
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.account.navigation.ProfileDestination
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.movie.ui.home.MovieScreen
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderNavArgs

fun NavGraphBuilder.movieHomeGraph(
    navController: NavController,
    openLibraryPage: () -> Unit
) = composable(
    destination = MovieHomeDestination,
    enterTransition = PeerEnterTransition,
    exitTransition = PeerExitTransition
) {
    MovieScreen(
        openMovieList = { listingArgs ->
            navController.navigate(MovieListingRoute(listingArgs))
        },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openSearchPage = {
            navController.navigateTo(SearchDestination.actualRoute)
        },
        openAccountPage = {
            navController.navigateTo(ProfileDestination.actualRoute)
        },
        openWatchProviderHub = { provider ->
            navController.navigateTo(
                WatchProviderHubDestination.actualRoute(
                    WatchProviderNavArgs(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = true
                    )
                )
            )
        },
        openLibraryPage = openLibraryPage
    )
}
