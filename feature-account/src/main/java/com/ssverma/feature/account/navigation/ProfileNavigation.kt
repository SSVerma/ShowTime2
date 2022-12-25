package com.ssverma.feature.account.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.account.ui.ProfileScreen
import com.ssverma.feature.auth.ui.AuthScreenContainer

fun NavGraphBuilder.profileGraph(
    navController: NavController
) = composable(destination = ProfileDestination) {

    AuthScreenContainer(
        onBackPressed = {
            navController.popBackStack()
        }
    ) {
        ProfileScreen(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}