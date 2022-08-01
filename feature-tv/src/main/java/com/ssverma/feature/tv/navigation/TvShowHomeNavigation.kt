package com.ssverma.feature.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.auth.navigation.AuthDestination
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.feature.tv.ui.TvShowScreen

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
        },
        openSearchPage = {
            navController.navigateTo(SearchDestination.actualRoute)
        },
        openAccountPage = {
            navController.navigateTo(AuthDestination.actualRoute)
        }
    )
}