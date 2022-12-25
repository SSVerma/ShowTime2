package com.ssverma.feature.person.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute

object PersonDetailDestination : DependentDestination<Int>("person") {
    const val PersonId = "personId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(PersonId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(PersonId, input)
            .build()
    }
}