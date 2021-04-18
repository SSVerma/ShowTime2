package com.ssverma.showtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssverma.showtime.ui.home.HomePage
import com.ssverma.showtime.ui.home.HomeViewModel

object AppDestinations {
    object BottomNavDestinations {
        const val MOVIE_ROUTE = "movie"
        const val TV_SHOW_ROUTE = "tv_show"
        const val PEOPLE_ROUTE = "people"
        const val LIBRARY_ROUTE = "library"
    }

    const val HOME_PAGE_ROUTE = "home_page"
}

@Composable
fun NavGraph(startDestination: String = AppDestinations.HOME_PAGE_ROUTE) {
    val navController = rememberNavController()

    val actions = remember(navController) { AppActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.HOME_PAGE_ROUTE) {
            val viewModel = hiltNavGraphViewModel<HomeViewModel>(it)
            HomePage(viewModel)
        }
    }
}

class AppActions(navController: NavHostController) {
    val onBackPress: () -> Unit = {
        navController.navigateUp()
    }
}