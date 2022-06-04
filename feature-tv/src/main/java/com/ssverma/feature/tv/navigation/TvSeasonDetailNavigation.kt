package com.ssverma.feature.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.feature.tv.ui.TvSeasonDetailsScreen

fun NavGraphBuilder.tvSeasonDetailGraph(
    navController: NavController
) = composable(destination = TvSeasonDetailDestination) {
    TvSeasonDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() },
        openEpisodeDetails = { episodeLaunchable ->
            navController.navigateTo(
                TvEpisodeDetailDestination.actualRoute(episodeLaunchable)
            )
        },
        openPersonDetails = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        }
    )
}