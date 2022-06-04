package com.ssverma.showtime.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.GraphDestination
import com.ssverma.core.navigation.navigation
import com.ssverma.feature.movie.navigation.MovieHomeDestination
import com.ssverma.feature.movie.navigation.movieHomeGraph
import com.ssverma.feature.person.navigation.personHomeGraph
import com.ssverma.showtime.ui.library.navigation.libraryHomeGraph
import com.ssverma.showtime.ui.tv.navigation.tvShowHomeGraph

object HomeDestination : GraphDestination("home")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.homeGraph(
    navController: NavController
) = navigation(
    graphDestination = HomeDestination,
    startDestination = MovieHomeDestination
) {
    movieHomeGraph(navController)
    tvShowHomeGraph(navController)
    personHomeGraph(navController)
    libraryHomeGraph()
}