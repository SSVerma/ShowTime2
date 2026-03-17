package com.ssverma.feature.movie.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.list.MovieListScreen
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderNavArgs

fun NavGraphBuilder.movieListGraph(
    navController: NavController
) = composable(MovieListDestination) {
    MovieListScreen(
        onBackPressed = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openWatchHub = { provider ->
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
        }
    )
}
