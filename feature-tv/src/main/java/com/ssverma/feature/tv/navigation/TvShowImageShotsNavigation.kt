package com.ssverma.feature.tv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.tv.ui.TvShowDetailsViewModel
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

fun NavGraphBuilder.tvShowImageShotsGraph(
    navController: NavController
) = composable(TvShowImageShotsDestination) {
    val tvShowDetailsViewModel: TvShowDetailsViewModel =
        navController.destinationViewModel(destination = TvShowDetailDestination)

    ImageShotsListScreen(
        imageShots = tvShowDetailsViewModel.imageShots,
        onBackPressed = { navController.popBackStack() },
        openImagePager = {
            navController.navigateTo(TvShowImagePagerDestination.actualRoute(it))
        }
    )
}