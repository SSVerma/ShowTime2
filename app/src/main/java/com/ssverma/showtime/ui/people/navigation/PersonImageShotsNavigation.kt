package com.ssverma.showtime.ui.people.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.core.navigation.composable
import com.ssverma.showtime.ui.people.PersonImageShotsScreen

object PersonImageShotsDestination : DependentDestination<Int>("person/images") {
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

fun NavGraphBuilder.personImageShotsGraph(
    navController: NavController
) = composable(destination = PersonImageShotsDestination) {
    PersonImageShotsScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
    )
}