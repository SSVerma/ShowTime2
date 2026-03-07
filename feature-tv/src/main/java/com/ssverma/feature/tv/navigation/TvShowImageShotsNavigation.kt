package com.ssverma.feature.tv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.tv.ui.details.TvShowImageShotsScreen

fun NavGraphBuilder.tvShowImageShotsGraph(
    navController: NavController
) = composable(destination = TvShowImageShotsDestination) {
    TvShowImageShotsScreen(
        navController = navController,
        onBackPressed = { navController.popBackStack() },
        openImagePager = { index ->
            navController.navigateTo(TvShowImagePagerDestination.actualRoute(index))
        }
    )
}
