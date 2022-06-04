package com.ssverma.feature.person.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.person.ui.PersonScreen
import com.ssverma.feature.tv.navigation.TvShowDetailDestination

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