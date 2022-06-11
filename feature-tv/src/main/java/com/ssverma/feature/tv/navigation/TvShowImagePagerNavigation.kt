package com.ssverma.feature.tv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen
import com.ssverma.feature.tv.ui.TvShowDetailsViewModel

fun NavGraphBuilder.tvShowImagePagerGraph(
    navController: NavController
) = composable(TvShowImagePagerDestination) {
    val tvShowDetailsViewModel = navController
        .destinationViewModel<TvShowDetailsViewModel>(destination = TvShowDetailDestination)

    ImagePagerScreen(
        imageShots = tvShowDetailsViewModel.imageShots,
        defaultPageIndex = it.arguments?.getInt(TvShowImagePagerDestination.PageIndex) ?: 0,
        onBackPressed = { navController.popBackStack() }
    )
}