package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.core.navigation.composable
import com.ssverma.showtime.ui.tv.TvShowReviewsScreen

object TvShowReviewsDestination : DependentDestination<Int>("tvShow/reviews") {
    const val TvShowId = "tvShowId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(TvShowId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(TvShowId, input)
            .build()
    }
}

fun NavGraphBuilder.tvShowReviewsGraph(
    navController: NavController
) = composable(destination = TvShowReviewsDestination) {
    TvShowReviewsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() }
    )
}