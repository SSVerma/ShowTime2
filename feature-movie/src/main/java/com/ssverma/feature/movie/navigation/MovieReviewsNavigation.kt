package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.movie.ui.MovieReviewsScreen

fun NavGraphBuilder.movieReviewsGraph(
    navController: NavController
) = composable(destination = MovieReviewsDestination) {
    MovieReviewsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() }
    )
}