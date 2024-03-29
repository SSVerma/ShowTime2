package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.account.navigation.ProfileDestination
import com.ssverma.feature.movie.ui.MovieScreen
import com.ssverma.feature.search.navigation.SearchDestination

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
        },
        openSearchPage = {
            navController.navigateTo(SearchDestination.actualRoute)
        },
        openAccountPage = {
            navController.navigateTo(ProfileDestination.actualRoute)
        }
    )
}