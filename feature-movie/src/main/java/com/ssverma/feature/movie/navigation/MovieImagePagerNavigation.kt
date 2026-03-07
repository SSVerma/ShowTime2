package com.ssverma.feature.movie.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.feature.movie.ui.details.MovieImagePagerScreen

fun NavGraphBuilder.movieImagePagerGraph(
    navController: NavController
) = composable(destination = MovieImagePagerDestination) { navBackStackEntry ->
    MovieImagePagerScreen(
        navController = navController,
        defaultPageIndex = navBackStackEntry.arguments?.getInt(MovieImagePagerDestination.PageIndex) ?: 0,
        onBackPressed = { navController.popBackStack() }
    )
}
