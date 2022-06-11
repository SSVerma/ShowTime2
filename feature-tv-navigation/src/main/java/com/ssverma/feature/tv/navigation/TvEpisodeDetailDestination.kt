package com.ssverma.feature.tv.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.feature.tv.navigation.args.TvEpisodeArgs

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