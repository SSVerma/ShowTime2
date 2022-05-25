package com.ssverma.showtime.ui.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.core.navigation.composable
import com.ssverma.showtime.ui.movie.MovieReviewsScreen

object MovieReviewsDestination : DependentDestination<Int>("movie/reviews") {
    const val MovieId = "movieId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(MovieId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(MovieId, input)
            .build()
    }
}

fun NavGraphBuilder.movieReviewsGraph(
    navController: NavController
) = composable(destination = MovieReviewsDestination) {
    MovieReviewsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() }
    )
}