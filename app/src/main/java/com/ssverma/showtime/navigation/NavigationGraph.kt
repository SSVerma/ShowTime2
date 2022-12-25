package com.ssverma.showtime.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.feature.account.navigation.profileGraph
import com.ssverma.feature.auth.navigation.authGraph
import com.ssverma.feature.movie.navigation.*
import com.ssverma.feature.person.navigation.personDetailGraph
import com.ssverma.feature.person.navigation.personImageShotsGraph
import com.ssverma.feature.search.navigation.searchGraph
import com.ssverma.feature.tv.navigation.*


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: StandaloneDestination = ShowTimeTopLevelDestination
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

        topLevelNavGraph(navController)

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

        searchGraph(navController)

        authGraph(navController)

        profileGraph(navController)
    }
}