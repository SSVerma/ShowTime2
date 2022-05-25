package com.ssverma.showtime.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.showtime.ui.home.HomeDestination
import com.ssverma.showtime.ui.home.homeGraph
import com.ssverma.showtime.ui.movie.navigation.*
import com.ssverma.showtime.ui.people.navigation.personDetailGraph
import com.ssverma.showtime.ui.people.navigation.personImageShotsGraph
import com.ssverma.showtime.ui.tv.navigation.*


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: StandaloneDestination = HomeDestination
) {
    val springStiffness = 900f

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination.placeholderRoute.asNavRoute(),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Up,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentScope.SlideDirection.Down,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Down,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        modifier = modifier
    ) {

        homeGraph(navController)

        movieListGraph(navController)
        movieDetailGraph(navController)
        movieImageShotsGraph(navController)
        movieImagePagerGraph(navController)
        movieReviewsGraph(navController)

        personDetailGraph(navController)
        personImageShotsGraph(navController)

        tvShowListGraph(navController)
        tvShowDetailGraph(navController)
        tvShowReviewsGraph(navController)
        tvShowImageShotsGraph(navController)
        tvShowImagePagerGraph(navController)

        tvSeasonDetailGraph(navController)
        tvEpisodeDetailGraph(navController)
    }
}