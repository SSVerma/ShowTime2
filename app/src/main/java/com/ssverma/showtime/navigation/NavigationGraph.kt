package com.ssverma.showtime.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.ssverma.core.navigation.StandaloneDestination
import com.ssverma.feature.account.navigation.profileGraph
import com.ssverma.feature.auth.navigation.authGraph
import com.ssverma.feature.movie.navigation.movieDetailGraph
import com.ssverma.feature.movie.navigation.movieImagePagerGraph
import com.ssverma.feature.movie.navigation.movieImageShotsGraph
import com.ssverma.feature.movie.navigation.movieListGraph
import com.ssverma.feature.movie.navigation.movieReviewsGraph
import com.ssverma.feature.person.navigation.personDetailGraph
import com.ssverma.feature.person.navigation.personImageShotsGraph
import com.ssverma.feature.search.navigation.searchGraph
import com.ssverma.feature.tv.navigation.tvEpisodeDetailGraph
import com.ssverma.feature.tv.navigation.tvSeasonDetailGraph
import com.ssverma.feature.tv.navigation.tvShowDetailGraph
import com.ssverma.feature.tv.navigation.tvShowImagePagerGraph
import com.ssverma.feature.tv.navigation.tvShowImageShotsGraph
import com.ssverma.feature.tv.navigation.tvShowListGraph
import com.ssverma.feature.tv.navigation.tvShowReviewsGraph


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: StandaloneDestination = ShowTimeTopLevelDestination
) {
    val springStiffness = 900f

    NavHost(
        navController = navController,
        startDestination = startDestination.placeholderRoute.asNavRoute(),
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = spring(stiffness = springStiffness)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
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