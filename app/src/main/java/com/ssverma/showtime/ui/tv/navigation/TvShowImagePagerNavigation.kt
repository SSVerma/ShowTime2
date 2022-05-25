package com.ssverma.showtime.ui.tv.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen
import com.ssverma.showtime.ui.tv.TvShowDetailsViewModel

object TvShowImagePagerDestination : DependentDestination<Int>("tvShow/imagePager") {
    const val PageIndex = "pageIndex"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(PageIndex, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(PageIndex, input)
            .build()
    }
}

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