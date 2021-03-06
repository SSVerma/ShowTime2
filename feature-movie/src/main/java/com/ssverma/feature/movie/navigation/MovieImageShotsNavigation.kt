package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.MovieDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

fun NavGraphBuilder.movieImageShotsGraph(
    navController: NavController
) = composable(destination = MovieImageShotsDestination) {
    val movieDetailsViewModel: MovieDetailsViewModel =
        navController.destinationViewModel(destination = MovieDetailDestination)

    ImageShotsListScreen(
        imageShots = movieDetailsViewModel.imageShots,
        onBackPressed = { navController.popBackStack() },
        openImagePager = {
            navController.navigateTo(MovieImagePagerDestination.actualRoute(it))
        }
    )
}