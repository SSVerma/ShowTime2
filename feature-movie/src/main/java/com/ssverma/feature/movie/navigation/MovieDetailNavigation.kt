package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.movie.ui.MovieDetailsScreen

object MovieDetailDestination : DependentDestination<Int>("movie") {
    const val ArgMovieId = "movieId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgMovieId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(ArgMovieId, input)
            .build()
    }
}

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
            //TODO: Uncomment once feature-person-navigation is up
//            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        openMovieList = { listingArgs ->
            navController.navigateTo(MovieListDestination.actualRoute(listingArgs))
        }
    )
}