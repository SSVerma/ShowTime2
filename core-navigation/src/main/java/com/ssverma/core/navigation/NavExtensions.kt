package com.ssverma.core.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.*
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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