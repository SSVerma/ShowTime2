package com.ssverma.showtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.MovieListScreen
import com.ssverma.showtime.ui.movie.MovieListViewModel
import com.ssverma.showtime.ui.movie.MovieListingType
import com.ssverma.showtime.ui.movie.MovieScreen
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen

object AppDestinations {
    object HomeBottomNavDestinations {
        const val MovieRoute = "movie"
        const val TvShowRoute = "tv-show"
        const val PeopleRoute = "people"
        const val LibraryRoute = "library"
    }

    const val HomePageRoute = "home-page"

    object MovieListDestination {
        private const val baseRoute = "movie-list"
        const val ArgType = "type"

        val arguments = listOf(
            navArgument(ArgType) { type = NavType.EnumType(MovieListingType::class.java) }
        )

        fun route(): String {
            return "$baseRoute/{$ArgType}"
        }

        fun launch(type: MovieListingType): String {
            return "$baseRoute/$type"
        }
    }
}

@Composable
fun NavGraph(
    modifier: Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.HomePageRoute
) {

    val actions = remember(navController) { AppActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        navigation(
            startDestination = AppDestinations.HomeBottomNavDestinations.MovieRoute,
            route = AppDestinations.HomePageRoute
        ) {
            composable(AppDestinations.HomeBottomNavDestinations.MovieRoute) {
                val viewModel = hiltViewModel<HomeViewModel>(it)
                MovieScreen(
                    viewModel = viewModel,
                    onNavigateToMovieList = { type ->
                        actions.navigateToMovieList(type)
                    }
                )
            }
            composable(AppDestinations.HomeBottomNavDestinations.TvShowRoute) {
                TvShowScreen()
            }
            composable(AppDestinations.HomeBottomNavDestinations.PeopleRoute) {
                PeopleScreen()
            }
            composable(AppDestinations.HomeBottomNavDestinations.LibraryRoute) {
                LibraryScreen()
            }
        }

        composable(
            route = AppDestinations.MovieListDestination.route(),
            arguments = AppDestinations.MovieListDestination.arguments,
        ) {
            val viewModel = hiltViewModel<MovieListViewModel>(it)

            MovieListScreen(
                viewModel = viewModel,
                onBackPressed = actions.onBackPress
            )
        }
    }
}

class AppActions(navController: NavHostController) {
    val onBackPress: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToMovieList: (type: MovieListingType) -> Unit = { type ->
        navController.navigate(
            AppDestinations.MovieListDestination.launch(type)
        )
    }
}