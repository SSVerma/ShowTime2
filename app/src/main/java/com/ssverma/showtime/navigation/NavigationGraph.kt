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
import com.ssverma.showtime.ui.movie.*
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
        startDestination = startDestination.placeholderRouteString(),
        modifier = modifier
    ) {

        val graphDestination = AppDestination.Home

        navigation(
            graphDestination = graphDestination,
            startDestination = AppDestination.HomeBottomNavDestination.Movie
        ) {
            composable(AppDestination.HomeBottomNavDestination.Movie) {
                val viewModel = hiltViewModel<HomeViewModel>(
                    navController.getBackStackEntry(graphDestination.placeholderRouteString())
                )
                MovieScreen(
                    viewModel = viewModel,
                    openMovieList = { launchable ->
                        navController.navigateTo(AppDestination.MovieList.actualRoute(launchable))
                    },
                    openMovieDetails = { movieId ->
                        navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
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
                onBackPressed = { navController.popBackStack() },
                openMovieDetails = { movieId ->
                    navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                }
            )
        }

        composable(AppDestination.MovieDetails) {
            val viewModel = hiltViewModel<MovieDetailsViewModel>(it)

            MovieDetailsScreen(
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() },
                openMovieDetails = { movieId ->
                    navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                }
            )
        }
    }
}

private fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRouteString(),
        arguments = destination.arguments(),
        content = content
    )
}

private fun NavGraphBuilder.navigation(
    graphDestination: Destination,
    startDestination: Destination,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRouteString(),
        startDestination = startDestination.placeholderRouteString(),
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asRoutableString()) {
        saveState()
    }
}

fun NavController.navigateTo(route: ActualRoute, builder: NavOptionsBuilder.() -> Unit) {
    navigate(
        route = route.asRoutableString(),
        builder = builder
    )
}

private fun Destination.placeholderRouteString(): String {
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