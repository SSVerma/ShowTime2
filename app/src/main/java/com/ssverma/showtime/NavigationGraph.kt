package com.ssverma.showtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.ssverma.showtime.ui.home.HomePage
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.movie.MovieListScreen
import com.ssverma.showtime.ui.movie.MovieListViewModel
import com.ssverma.showtime.ui.movie.MovieListingType

object AppDestinations {
    object BottomNavDestinations {
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

        fun to(type: MovieListingType): String {
            return "$baseRoute/$type"
        }
    }
}

@Composable
fun NavGraph(startDestination: String = AppDestinations.HomePageRoute) {
    val navController = rememberNavController()

    val actions = remember(navController) { AppActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(AppDestinations.HomePageRoute) {
            val viewModel = hiltNavGraphViewModel<HomeViewModel>(it)
            HomePage(viewModel, actions)
        }

        composable(
            route = AppDestinations.MovieListDestination.route(),
            arguments = AppDestinations.MovieListDestination.arguments,
        ) {
            val viewModel = hiltNavGraphViewModel<MovieListViewModel>(it)

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
            AppDestinations.MovieListDestination.to(type)
        )
    }
}