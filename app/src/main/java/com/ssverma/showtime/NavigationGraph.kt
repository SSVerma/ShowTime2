package com.ssverma.showtime

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.ssverma.showtime.ui.home.HomePage
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.movie.MovieListScreen
import com.ssverma.showtime.ui.movie.MovieListViewModel

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
        const val ArgTitle = "title"
        const val ArgType = "type"

        val arguments = listOf(
            navArgument(ArgTitle) { type = NavType.IntType },
            navArgument(ArgType) { type = NavType.StringType }
        )

        fun route(): String {
            return "$baseRoute/{$ArgTitle}/{$ArgType}"
        }

        fun to(@StringRes title: Int, type: String): String {
            return "$baseRoute/$title/$type"
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

            val titleRes = it.arguments?.getInt(AppDestinations.MovieListDestination.ArgTitle) ?: 0
            val type = it.arguments?.getString(AppDestinations.MovieListDestination.ArgType)

            val viewModel = hiltNavGraphViewModel<MovieListViewModel>(it)

            MovieListScreen(
                title = stringResource(id = titleRes),
                type = type!!,
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

    val navigateToMovieList: (titleRes: Int, type: String) -> Unit = { titleRes, type ->
        navController.navigate(
            AppDestinations.MovieListDestination.to(titleRes, type)
        )
    }
}