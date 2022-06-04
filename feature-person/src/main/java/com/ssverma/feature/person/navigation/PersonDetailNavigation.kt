package com.ssverma.feature.person.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.*
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.person.ui.PersonDetailsScreen

fun NavGraphBuilder.personDetailGraph(
    navController: NavController
) = composable(destination = PersonDetailDestination) {
    PersonDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openTvShowDetails = { tvShowId ->
            //TODO until tv show navigation module is up
//            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openPersonAllImages = { personId ->
            navController.navigateTo(PersonImageShotsDestination.actualRoute(personId))
        }
    )
}