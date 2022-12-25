package com.ssverma.feature.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.feature.tv.ui.TvShowDetailsScreen

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