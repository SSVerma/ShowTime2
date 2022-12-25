package com.ssverma.feature.tv.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute

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