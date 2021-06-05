package com.ssverma.showtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.MovieListScreen
import com.ssverma.showtime.ui.movie.MovieListViewModel
import com.ssverma.showtime.ui.movie.MovieScreen
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen


@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: StandaloneDestination = AppDestination.Home //TODO: Make Destination when start dest arg supported in nav compose
) {

    NavHost(
        navController = navController,
        startDestination = startDestination.placeholderRoute(),
        modifier = modifier
    ) {

        navigation(
            graphDestination = AppDestination.Home,
            startDestination = AppDestination.HomeBottomNavDestination.Movie
        ) {
            composable(AppDestination.HomeBottomNavDestination.Movie) {
                val viewModel = hiltViewModel<HomeViewModel>(it)
                MovieScreen(
                    viewModel = viewModel,
                    onNavigateToMovieList = { launchable ->
                        navController.navigateTo(AppDestination.MovieList.actualRoute(launchable))
                    }
                )
            }

            composable(AppDestination.HomeBottomNavDestination.Tv) {
                TvShowScreen()
            }

            composable(AppDestination.HomeBottomNavDestination.People) {
                PeopleScreen()
            }

            composable(AppDestination.HomeBottomNavDestination.Library) {
                LibraryScreen()
            }
        }

        composable(AppDestination.MovieList) {
            val viewModel = hiltViewModel<MovieListViewModel>(it)

            MovieListScreen(
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}

private fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRoute(),
        arguments = destination.arguments(),
        content = content
    )
}

//private fun <T> NavGraphBuilder.composable(
//    destination: DependentDestination<T>,
//    content: @Composable (NavBackStackEntry) -> Unit
//) {
//    composable(
//        route = destination.placeholderRoute.asRoutableString(),
//        arguments = destination.arguments(),
//        content = content
//    )
//}

//private fun <T> NavGraphBuilder.navigation(
//    destination: DependentDestination<T>,
//    startDestination: DependentDestination<T>,
//    builder: NavGraphBuilder.() -> Unit
//) {
//    navigation(
//        route = destination.placeholderRoute.asRoutableString(),
//        startDestination = startDestination.placeholderRoute.asRoutableString(),
//        builder = builder
//    )
//}

private fun NavGraphBuilder.navigation(
    graphDestination: Destination,
    startDestination: Destination,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRoute(),
        startDestination = startDestination.placeholderRoute(),
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asRoutableString())
}

fun NavController.navigateTo(route: ActualRoute, builder: NavOptionsBuilder.() -> Unit) {
    navigate(
        route = route.asRoutableString(),
        builder = builder
    )
}

private fun Destination.placeholderRoute(): String {
    return when (this) {
        is StandaloneDestination -> {
            placeholderRoute.asRoutableString()
        }
        is DependentDestination<*> -> {
            placeholderRoute.asRoutableString()
        }
        else -> identifier
    }
}

private fun Destination.arguments(): List<NamedNavArgument> {
    return if (this is DependentDestination<*>) arguments() else emptyList()
}