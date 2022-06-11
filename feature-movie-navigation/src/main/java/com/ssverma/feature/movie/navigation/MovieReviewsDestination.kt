package com.ssverma.feature.movie.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute

object MovieReviewsDestination : DependentDestination<Int>("movie/reviews") {
    const val MovieId = "movieId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(MovieId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(MovieId, input)
            .build()
    }
}