package com.ssverma.feature.person.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.person.ui.PersonImageShotsScreen

fun NavGraphBuilder.personImageShotsGraph(
    navController: NavController
) = composable(destination = PersonImageShotsDestination) {
    PersonImageShotsScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
    )
}