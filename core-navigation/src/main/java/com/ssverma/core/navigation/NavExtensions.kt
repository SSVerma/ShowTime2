package com.ssverma.core.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.*
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

@Composable
inline fun <reified VM : ViewModel> NavController.destinationViewModel(destination: Destination): VM {
    return hiltViewModel(
        remember { getBackStackEntry(destination.placeholderRoute.asNavRoute()) }
    )
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRoute.asNavRoute(),
        arguments = destination.arguments,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
inline fun <reified VM : ViewModel> NavGraphBuilder.composable(
    graphDestination: GraphDestination,
    destination: Destination,
    navController: NavHostController,
    crossinline content: @Composable (navGraphElement: NavGraphElement<VM>) -> Unit
) {
    composable(
        route = destination.placeholderRoute.asNavRoute(),
        arguments = destination.arguments,
    ) {
        val viewModel = navController.destinationViewModel<VM>(destination = graphDestination)
        content(
            NavGraphElement(
                graphScopedViewModel = viewModel,
                navBackStackEntry = it
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.navigation(
    graphDestination: GraphDestination,
    startDestination: Destination,
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRoute.asNavRoute(),
        startDestination = startDestination.placeholderRoute.asNavRoute(),
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asNavRoute())
}

fun NavController.navigateTo(route: ActualRoute, builder: NavOptionsBuilder.() -> Unit) {
    navigate(
        route = route.asNavRoute(),
        builder = builder
    )
}

data class NavGraphElement<T : ViewModel>(
    val graphScopedViewModel: T,
    val navBackStackEntry: NavBackStackEntry
)