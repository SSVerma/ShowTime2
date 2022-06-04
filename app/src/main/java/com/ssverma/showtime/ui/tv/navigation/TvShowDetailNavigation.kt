package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.showtime.ui.tv.TvShowDetailsScreen

object TvShowDetailDestination : DependentDestination<Int>("tvShow") {
    const val ArgTvShowId = "tvShowId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(ArgTvShowId, input)
            .build()
    }
}

fun NavGraphBuilder.tvShowDetailGraph(
    navController: NavController
) = composable(destination = TvShowDetailDestination) {
    TvShowDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openImageShotsList = {
            navController.navigateTo(TvShowImageShotsDestination.actualRoute)
        },
        openImageShot = { index ->
            navController.navigateTo(TvShowImagePagerDestination.actualRoute(index))
        },
        openReviewsList = { tvShowId ->
            navController.navigateTo(TvShowReviewsDestination.actualRoute(tvShowId))
        },
        openPersonDetails = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        },
        openTvShowList = { listingArgs ->
            navController.navigateTo(TvShowListDestination.actualRoute(listingArgs))
        },
        openTvSeasonDetails = { seasonLaunchable ->
            navController.navigateTo(
                TvSeasonDetailDestination.actualRoute(seasonLaunchable)
            )
        }
    )
}