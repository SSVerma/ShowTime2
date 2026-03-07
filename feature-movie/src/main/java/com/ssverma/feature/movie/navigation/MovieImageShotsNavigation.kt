package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.details.MovieImageShotsScreen

fun NavGraphBuilder.movieImageShotsGraph(
    navController: NavController
) = composable(destination = MovieImageShotsDestination) {
    MovieImageShotsScreen(
        navController = navController,
        onBackPressed = { navController.popBackStack() },
        openImagePager = { index ->
            navController.navigateTo(MovieImagePagerDestination.actualRoute(index))
        }
    )
}
