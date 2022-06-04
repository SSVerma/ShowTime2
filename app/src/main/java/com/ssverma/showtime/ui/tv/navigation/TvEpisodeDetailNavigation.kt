package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.person.navigation.PersonDetailDestination
import com.ssverma.showtime.ui.tv.TvEpisodeArgs
import com.ssverma.showtime.ui.tv.TvEpisodeDetailsScreen

object TvEpisodeDetailDestination : DependentDestination<TvEpisodeArgs>("tvShow/season/episode") {
    const val ArgTvShowId = "tvShowId"
    const val ArgSeasonNumber = "seasonNumber"
    const val ArgEpisodeNumber = "episodeNumber"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
            .mandatoryArg(ArgSeasonNumber, navType = NavType.IntType)
            .mandatoryArg(ArgEpisodeNumber, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(
        input: TvEpisodeArgs,
        builder: ActualRoute.ActualRouteBuilder
    ): ActualRoute {
        return builder
            .mandatoryArg(ArgTvShowId, input.tvShowId)
            .mandatoryArg(ArgSeasonNumber, input.seasonNumber)
            .mandatoryArg(ArgEpisodeNumber, input.episodeNumber)
            .build()
    }
}

fun NavGraphBuilder.tvEpisodeDetailGraph(
    navController: NavController
) = composable(destination = TvEpisodeDetailDestination) {
    TvEpisodeDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() },
        openPersonDetails = { personId ->
            navController.navigateTo(PersonDetailDestination.actualRoute(personId))
        }
    )
}