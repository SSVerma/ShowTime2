package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.movie.ui.MovieDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen

fun NavGraphBuilder.movieImagePagerGraph(
    navController: NavController
) = composable(destination = MovieImagePagerDestination) {

    val movieDetailsViewModel = navController
        .destinationViewModel<MovieDetailsViewModel>(destination = MovieDetailDestination)

    ImagePagerScreen(
        imageShots = movieDetailsViewModel.imageShots,
        defaultPageIndex = it.arguments?.getInt(MovieImagePagerDestination.PageIndex) ?: 0,
        onBackPressed = { navController.popBackStack() }
    )
}