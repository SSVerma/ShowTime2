package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.ui.MovieScreen

fun NavGraphBuilder.movieHomeGraph(
    navController: NavController
) = composable(destination = MovieHomeDestination) {
    MovieScreen(
        viewModel = hiltViewModel(),
        openMovieList = { listingArgs ->
            navController.navigateTo(MovieListDestination.actualRoute(listingArgs))
        },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        }
    )
}