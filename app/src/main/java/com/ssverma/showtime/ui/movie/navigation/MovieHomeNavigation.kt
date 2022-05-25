package com.ssverma.showtime.ui.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.showtime.ui.movie.MovieScreen

object MovieHomeDestination : StandaloneDestination("movie/home")

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