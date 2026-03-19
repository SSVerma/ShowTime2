package com.ssverma.feature.movie.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.movie.ui.details.MovieDetailsScreen
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderNavArgs

fun NavGraphBuilder.movieDetailGraph(
    navController: NavController
) = composable(destination = MovieDetailDestination) {
    MovieDetailsScreen(
        onBackPressed = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openImageShotsList = {
            navController.navigateTo(MovieImageShotsDestination.actualRoute)
        },
        openImageShot = { index ->
            navController.navigateTo(MovieImagePagerDestination.actualRoute(index))
        },
        openReviewsList = { movieId ->
            navController.navigateTo(MovieReviewsDestination.actualRoute(movieId))
        },
        openPersonDetails = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        openMovieList = { listingArgs ->
            navController.navigate(MovieListingRoute(listingArgs))
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
