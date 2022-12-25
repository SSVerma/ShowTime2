package com.ssverma.feature.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.tv.ui.TvShowListScreen

fun NavGraphBuilder.tvShowListGraph(
    navController: NavController
) = composable(destination = TvShowListDestination) {
    TvShowListScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        }
    )
}