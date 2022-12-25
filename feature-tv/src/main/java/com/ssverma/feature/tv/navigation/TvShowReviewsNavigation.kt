package com.ssverma.feature.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.tv.ui.TvShowReviewsScreen

fun NavGraphBuilder.tvShowReviewsGraph(
    navController: NavController
) = composable(destination = TvShowReviewsDestination) {
    TvShowReviewsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() }
    )
}