package com.ssverma.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.putResultAndPopCurrentDestination
import com.ssverma.core.navigation.putResultForPreviousDestination
import com.ssverma.feature.auth.ui.AuthScreen

fun NavGraphBuilder.authGraph(
    navController: NavController
) = composable(destination = AuthDestination) {
    AuthScreen(
        onAuthSessionEstablished = {
            navController.putResultForPreviousDestination(
                resultKey = AuthDestination.resultKey,
                resultValue = true
            )
            navController.popBackStack(
                route = AuthDestination.actualRoute.asNavRoute(),
                inclusive = true
            )
        },
        onBackPressed = {
            navController.putResultAndPopCurrentDestination(
                resultKey = AuthDestination.resultKey,
                resultValue = false
            )
        }
    )
}