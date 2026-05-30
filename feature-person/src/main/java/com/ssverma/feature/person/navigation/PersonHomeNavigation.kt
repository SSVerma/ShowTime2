package com.ssverma.feature.person.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.PeerEnterTransition
import com.ssverma.core.navigation.PeerExitTransition
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.account.navigation.ProfileDestination
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.feature.person.ui.home.PersonScreen
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.feature.tv.navigation.TvShowDetailDestination

fun NavGraphBuilder.personHomeGraph(
    navController: NavController,
    openLibraryPage: () -> Unit
) = composable(
    destination = PersonHomeDestination,
    enterTransition = PeerEnterTransition,
    exitTransition = PeerExitTransition
) {
    PersonScreen(
        openPersonDetailsScreen = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        openMovieDetailsScreen = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openTvShowDetailsScreen = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openSearchPage = {
            navController.navigateTo(SearchDestination.actualRoute)
        },
        openAccountPage = {
            navController.navigateTo(ProfileDestination.actualRoute)
        },
        openLibraryPage = openLibraryPage
    )
}