package com.ssverma.feature.person.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.person.ui.PersonDetailsScreen
import com.ssverma.feature.tv.navigation.TvShowDetailDestination

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
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openPersonAllImages = { personId ->
            navController.navigateTo(PersonImageShotsDestination.actualRoute(personId))
        }
    )
}