package com.ssverma.feature.tv.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.tv.ui.details.TvShowDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen

fun NavGraphBuilder.tvShowImagePagerGraph(
    navController: NavController
) = composable(destination = TvShowImagePagerDestination) { navBackStackEntry ->
    val tvShowDetailsViewModel = navController
        .destinationViewModel<TvShowDetailsViewModel>(destination = TvShowDetailDestination)

    val uiState by tvShowDetailsViewModel.uiState.collectAsStateWithLifecycle()

    ImagePagerScreen(
        imageShots = uiState.imageShots,
        defaultPageIndex = navBackStackEntry.arguments?.getInt(TvShowImagePagerDestination.PageIndex) ?: 0,
        onBackPressed = { navController.popBackStack() }
    )
}
