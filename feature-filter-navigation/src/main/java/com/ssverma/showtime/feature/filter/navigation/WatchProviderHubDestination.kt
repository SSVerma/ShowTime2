package com.ssverma.showtime.feature.filter.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute

object WatchProviderHubDestination :
    DependentDestination<WatchProviderNavArgs>("watch-provider-hub") {

    const val ArgProviderId = "providerId"
    const val ArgProviderName = "providerName"
    const val ArgLogoPath = "logoPath"
    const val ArgIsMovie = "isMovie"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(ArgProviderId, NavType.IntType)
            .mandatoryArg(ArgProviderName, NavType.StringType)
            .mandatoryArg(ArgLogoPath, NavType.StringType)
            .mandatoryArg(ArgIsMovie, NavType.BoolType)
            .build()
    }

    override fun actualRoute(
        input: WatchProviderNavArgs,
        builder: ActualRoute.ActualRouteBuilder
    ): ActualRoute {
        return builder
            .mandatoryArg(ArgProviderId, input.providerId)
            .mandatoryArg(ArgProviderName, input.providerName)
            .mandatoryArg(ArgLogoPath, input.logoPath)
            .mandatoryArg(ArgIsMovie, input.isMovie)
            .build()
    }
}
