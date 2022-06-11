package com.ssverma.feature.tv.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.feature.tv.navigation.args.TvSeasonArgs

object TvSeasonDetailDestination : DependentDestination<TvSeasonArgs>("tvShow/season") {
    const val ArgTvShowId = "tvShowId"
    const val ArgTvSeasonNumber = "seasonNumber"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
            .mandatoryArg(ArgTvSeasonNumber, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(
        input: TvSeasonArgs,
        builder: ActualRoute.ActualRouteBuilder
    ): ActualRoute {
        return builder
            .mandatoryArg(ArgTvShowId, input.tvShowId)
            .mandatoryArg(ArgTvSeasonNumber, input.seasonNumber)
            .build()
    }
}