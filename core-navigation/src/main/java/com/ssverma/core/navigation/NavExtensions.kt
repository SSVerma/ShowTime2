package com.ssverma.core.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

val PeerEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    fadeIn(animationSpec = tween(220, delayMillis = 90)) +
    scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90))
}

val PeerExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    fadeOut(animationSpec = tween(90))
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
inline fun <reified VM : ViewModel> NavController.destinationViewModel(destination: Destination): VM {
    return hiltViewModel(
        remember { getBackStackEntry(destination.placeholderRoute.asNavRoute()) }
    )
}


fun NavGraphBuilder.composable(
    destination: Destination,
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    this.composable(
        route = destination.placeholderRoute.asNavRoute(),
        arguments = destination.arguments,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}

inline fun <reified VM : ViewModel> NavGraphBuilder.composable(
    graphDestination: GraphDestination,
    destination: Destination,
    navController: NavHostController,
    crossinline content: @Composable (navGraphElement: NavGraphElement<VM>) -> Unit
) {
    this.composable(
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
    enterTransition: (AnimatedContentScope.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope.() -> ExitTransition?)? = exitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRoute.asNavRoute(),
        startDestination = startDestination.placeholderRoute.asNavRoute(),
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

/**
 * @param coroutineScope: scope should live longer than the destination of [route]
 */
fun <T> NavController.navigateForResult(
    route: ActualRoute,
    resultKey: String,
    coroutineScope: CoroutineScope,
    initialResultValue: T? = null,
    onResult: (result: T) -> Unit
) {
    val resultFlow = currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow(key = resultKey, initialValue = initialResultValue)

    // order is important, should be done after above statement otherwise current
    // entry would become previous after navigation.
    navigateTo(route = route)

    coroutineScope.launch {
        val result = resultFlow?.firstOrNull(predicate = { it != null })
        result?.let {
            onResult(it)
            currentBackStackEntry?.savedStateHandle?.remove<T>(resultKey)
        }
    }
}

fun <T> NavController.putResultForPreviousDestination(
    resultKey: String,
    resultValue: T
) {
    previousBackStackEntry?.savedStateHandle?.set(resultKey, resultValue)
}

fun <T> NavController.putResultAndPopCurrentDestination(
    resultKey: String,
    resultValue: T
) {
    putResultForPreviousDestination(resultKey, resultValue)
    popBackStack()
}

@Composable
fun <T> NavController.NavigationResult(
    resultKey: String,
    onResult: (result: T) -> Unit
) {
    val currentOnResult by rememberUpdatedState(newValue = onResult)

    val result = currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<T?>(key = resultKey, initialValue = null)
        ?.collectAsState()?.value

    result?.let {
        currentOnResult(it)
        currentBackStackEntry?.savedStateHandle?.remove<T>(resultKey)
    }
}

@Composable
fun <T> NavController.NavigationResult(
    resultKey: String,
    onResult: (result: T?) -> Unit,
    pendingNavigationProvider: ((route: ActualRoute) -> Unit) -> Unit
) {
    pendingNavigationProvider { navigateTo(it) }

    NavigationResult(resultKey = resultKey, onResult = onResult)
}

data class NavGraphElement<T : ViewModel>(
    val graphScopedViewModel: T,
    val navBackStackEntry: NavBackStackEntry
)