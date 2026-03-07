package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.movie.ui.details.MovieReviewsScreen

fun NavGraphBuilder.movieReviewsGraph(
    navController: NavController
) = composable(destination = MovieReviewsDestination) {
    MovieReviewsScreen(
        onBackPress = { navController.popBackStack() }
    )
}
