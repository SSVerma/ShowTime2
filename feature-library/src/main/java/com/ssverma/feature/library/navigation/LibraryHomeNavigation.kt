package com.ssverma.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.account.navigation.ProfileDestination
import com.ssverma.feature.auth.ui.AuthScreenContainer
import com.ssverma.feature.library.ui.LibraryScreen
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.feature.tv.navigation.TvShowDetailDestination

fun NavGraphBuilder.libraryHomeGraph(
    navController: NavController
) = composable(destination = LibraryHomeDestination) {
    AuthScreenContainer(onBackPressed = { navController.popBackStack() }) {
        LibraryScreen(
            onMovieClicked = { movieId ->
                navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
            },
            onTvShowClicked = { tvShowId ->
                navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
            },
            openSearchPage = {
                navController.navigateTo(SearchDestination.actualRoute)
            },
            openAccountPage = {
                navController.navigateTo(ProfileDestination.actualRoute)
            }
        )
    }
}