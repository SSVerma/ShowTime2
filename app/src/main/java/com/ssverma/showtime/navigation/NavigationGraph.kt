package com.ssverma.showtime.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NamedNavArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.ImageShotsListScreen
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.*
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: StandaloneDestination = AppDestination.Home
) {

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination.placeholderRouteString(),
        enterTransition = { _, _ ->
            slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(300))
        },
        exitTransition = { _, _ ->
            slideOutOfContainer(
                AnimatedContentScope.SlideDirection.Down,
                animationSpec = tween(280)
            )
        },
        popEnterTransition = { _, _ ->
            slideIntoContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(300))
        },
//        popExitTransition = { _, _ ->
//            slideOutOfContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(700))
//        },
        modifier = modifier
    ) {

        navigation(
            graphDestination = AppDestination.Home,
            startDestination = AppDestination.HomeBottomNavDestination.Movie
        ) {
            composable<HomeViewModel>(
                graphDestination = AppDestination.Home,
                destination = AppDestination.HomeBottomNavDestination.Movie,
                navController = navController
            ) {
                MovieScreen(
                    viewModel = it.graphScopedViewModel,
                    openMovieList = { launchable ->
                        navController.navigateTo(AppDestination.MovieList.actualRoute(launchable))
                    },
                    openMovieDetails = { movieId ->
                        navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                    }
                )
            }

            composable<HomeViewModel>(
                graphDestination = AppDestination.Home,
                destination = AppDestination.HomeBottomNavDestination.Tv,
                navController = navController
            ) {
                TvShowScreen()
            }

            composable<HomeViewModel>(
                graphDestination = AppDestination.Home,
                destination = AppDestination.HomeBottomNavDestination.People,
                navController = navController
            ) {
                PeopleScreen()
            }

            composable<HomeViewModel>(
                graphDestination = AppDestination.Home,
                destination = AppDestination.HomeBottomNavDestination.Library,
                navController = navController
            ) {
                LibraryScreen()
            }
        }

        composable(AppDestination.MovieList) {
            val viewModel = hiltViewModel<MovieListViewModel>(it)

            MovieListScreen(
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() },
                openMovieDetails = { movieId ->
                    navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                }
            )
        }

        composable(destination = AppDestination.MovieDetails) {
            MovieDetailsScreen(
                viewModel = hiltViewModel(),
                onBackPressed = { navController.popBackStack() },
                openMovieDetails = { movieId ->
                    navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                },
                openImageShotsList = {
                    navController.navigateTo(AppDestination.ImageShots.actualRoute)
                },
                openImageShot = { index ->
                    navController.navigateTo(AppDestination.ImagePager.actualRoute(index))
                },
                openReviewsList = { movieId ->
                    navController.navigateTo(AppDestination.MovieReviews.actualRoute(movieId))
                }
            )
        }

        composable(AppDestination.ImageShots) {
            val movieDetailsViewModel = hiltViewModel<MovieDetailsViewModel>(
                navController.getBackStackEntry(
                    AppDestination.MovieDetails.placeholderRouteString()
                )
            )
            ImageShotsListScreen(
                liveImageShots = movieDetailsViewModel.imageShots,
                onBackPressed = { navController.popBackStack() },
                openImagePager = {
                    navController.navigateTo(AppDestination.ImagePager.actualRoute(it))
                }
            )
        }

        composable(AppDestination.ImagePager) {
            val movieDetailsViewModel = hiltViewModel<MovieDetailsViewModel>(
                navController.getBackStackEntry(
                    AppDestination.MovieDetails.placeholderRouteString()
                )
            )

            ImagePagerScreen(
                liveImageShots = movieDetailsViewModel.imageShots,
                defaultPageIndex = it.arguments?.getInt(AppDestination.ImagePager.PageIndex)
                    ?: 0,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(destination = AppDestination.MovieReviews) {
            val viewModel = hiltViewModel<MovieReviewsViewModel>(it)
            MovieReviewsScreen(
                viewModel = viewModel,
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRouteString(),
        arguments = destination.arguments(),
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
private inline fun <reified T : ViewModel> NavGraphBuilder.composable(
    graphDestination: GraphDestination,
    destination: Destination,
    navController: NavHostController,
    crossinline content: @Composable (navGraphElement: NavGraphElement<T>) -> Unit
) {
    composable(
        route = destination.placeholderRouteString(),
        arguments = destination.arguments(),
    ) {
        val viewModel = hiltViewModel<T>(
//            it
            navController.getBackStackEntry(graphDestination.placeholderRouteString())
        )
        content(
            NavGraphElement(
                graphScopedViewModel = viewModel,
                navBackStackEntry = it
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.navigation(
    graphDestination: GraphDestination,
    startDestination: Destination,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRouteString(),
        startDestination = startDestination.placeholderRouteString(),
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asRoutableString())
}

fun NavController.navigateTo(route: ActualRoute, builder: NavOptionsBuilder.() -> Unit) {
    navigate(
        route = route.asRoutableString(),
        builder = builder
    )
}

private fun Destination.placeholderRouteString(): String {
    return when (this) {
        is StandaloneDestination -> {
            placeholderRoute.asRoutableString()
        }
        is DependentDestination<*> -> {
            placeholderRoute.asRoutableString()
        }
        is GraphDestination -> {
            placeholderRoute.asRoutableString()
        }
        else -> throw IllegalStateException("Invalid destination")
    }
}

private fun Destination.arguments(): List<NamedNavArgument> {
    return if (this is DependentDestination<*>) arguments() else emptyList()
}

class NavGraphElement<T : ViewModel>(
    val graphScopedViewModel: T,
    val navBackStackEntry: NavBackStackEntry
)