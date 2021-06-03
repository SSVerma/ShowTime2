package com.ssverma.showtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
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
    startDestination: String = AppDestination.Home.placeholderRoute.asRoutableString()
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        navigation(
            destination = AppDestination.Home,
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

private fun <T> NavGraphBuilder.composable(
    destination: Destination<T>,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRoute.asRoutableString(),
        arguments = destination.arguments(),
        content = content
    )
}

private fun <T> NavGraphBuilder.navigation(
    destination: Destination<T>,
    startDestination: Destination<T>,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = destination.placeholderRoute.asRoutableString(),
        startDestination = startDestination.placeholderRoute.asRoutableString(),
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asRoutableString())
}