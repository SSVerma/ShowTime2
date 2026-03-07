package com.ssverma.feature.tv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.tv.ui.details.TvShowImagePagerScreen

fun NavGraphBuilder.tvShowImagePagerGraph(
    navController: NavController
) = composable(destination = TvShowImagePagerDestination) { navBackStackEntry ->
    TvShowImagePagerScreen(
        navController = navController,
        defaultPageIndex = navBackStackEntry.arguments?.getInt(TvShowImagePagerDestination.PageIndex) ?: 0,
        onBackPressed = { navController.popBackStack() }
    )
}
