package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.movie.ui.MovieDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen

object MovieImagePagerDestination : DependentDestination<Int>("movie/imagePager") {
    const val PageIndex = "pageIndex"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(PageIndex, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(PageIndex, input)
            .build()
    }
}

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