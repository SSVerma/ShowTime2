package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.MovieListScreen

fun NavGraphBuilder.movieListGraph(
    navController: NavController
) = composable(MovieListDestination) {
    MovieListScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        }
    )
}