package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.showtime.ui.tv.TvSeasonDetailsScreen
import com.ssverma.showtime.ui.tv.TvSeasonLaunchable

object TvSeasonDetailDestination : DependentDestination<TvSeasonLaunchable>("tvShow/season") {
    const val ArgTvShowId = "tvShowId"
    const val ArgTvSeasonNumber = "seasonNumber"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
            .mandatoryArg(ArgTvSeasonNumber, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(
        input: TvSeasonLaunchable,
        builder: ActualRoute.ActualRouteBuilder
    ): ActualRoute {
        return builder
            .mandatoryArg(ArgTvShowId, input.tvShowId)
            .mandatoryArg(ArgTvSeasonNumber, input.seasonNumber)
            .build()
    }
}

fun NavGraphBuilder.tvSeasonDetailGraph(
    navController: NavController
) = composable(destination = TvSeasonDetailDestination) {
    TvSeasonDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() },
        openEpisodeDetails = { episodeLaunchable ->
            navController.navigateTo(
                TvEpisodeDetailDestination.actualRoute(episodeLaunchable)
            )
        },
        openPersonDetails = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        }
    )
}