package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.MovieDetailsScreen
import com.ssverma.feature.person.navigation.PersonDetailDestination

fun NavGraphBuilder.movieDetailGraph(
    navController: NavController
) = composable(destination = MovieDetailDestination) {
    MovieDetailsScreen(
        viewModel = hiltViewModel(),
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
            navController.navigateTo(MovieListDestination.actualRoute(listingArgs))
        }
    )
}