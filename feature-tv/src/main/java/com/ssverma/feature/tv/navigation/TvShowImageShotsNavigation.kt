package com.ssverma.feature.tv.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.tv.ui.details.TvShowDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

fun NavGraphBuilder.tvShowImageShotsGraph(
    navController: NavController
) = composable(destination = TvShowImageShotsDestination) {
    val tvShowDetailsViewModel = navController
        .destinationViewModel<TvShowDetailsViewModel>(destination = TvShowDetailDestination)

    val uiState by tvShowDetailsViewModel.uiState.collectAsStateWithLifecycle()

    ImageShotsListScreen(
        imageShots = uiState.imageShots,
        onBackPressed = { navController.popBackStack() },
        openImagePager = { index ->
            navController.navigateTo(TvShowImagePagerDestination.actualRoute(index))
        }
    )
}
