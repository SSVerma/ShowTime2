package com.ssverma.showtime.ui.people.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.showtime.ui.movie.navigation.MovieDetailDestination
import com.ssverma.showtime.ui.people.PersonScreen
import com.ssverma.showtime.ui.tv.navigation.TvShowDetailDestination

object PersonHomeDestination : StandaloneDestination("person/home")

fun NavGraphBuilder.personHomeGraph(
    navController: NavController
) = composable(destination = PersonHomeDestination) {
    PersonScreen(
        viewModel = hiltViewModel(),
        openPersonDetailsScreen = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        openMovieDetailsScreen = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openTvShowDetailsScreen = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        }
    )
}