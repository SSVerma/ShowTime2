package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.showtime.ui.tv.TvShowScreen

object TvShowHomeDestination : StandaloneDestination("tvShow/home")

fun NavGraphBuilder.tvShowHomeGraph(
    navController: NavController
) = composable(destination = TvShowHomeDestination) {
    TvShowScreen(
        viewModel = hiltViewModel(),
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openTvShowList = { listingArgs ->
            navController.navigateTo(
                TvShowListDestination.actualRoute(listingArgs)
            )
        }
    )
}