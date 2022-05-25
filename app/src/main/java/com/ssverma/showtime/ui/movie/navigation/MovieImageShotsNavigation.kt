package com.ssverma.showtime.ui.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.core.navigation.navigateTo
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen
import com.ssverma.showtime.ui.movie.MovieDetailsViewModel

object MovieImageShotsDestination : StandaloneDestination("movie/shots/image")

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