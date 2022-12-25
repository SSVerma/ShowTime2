package com.ssverma.feature.search.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.feature.search.ui.SearchSuggestionScreen
import com.ssverma.feature.tv.navigation.TvShowDetailDestination

fun NavGraphBuilder.searchGraph(
    navController: NavController
) = composable(destination = SearchDestination) {
    SearchSuggestionScreen(
        onMovieClick = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        onTvShowClick = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        onPersonClick = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        onBackPressed = {
            navController.popBackStack()
        }
    )
}