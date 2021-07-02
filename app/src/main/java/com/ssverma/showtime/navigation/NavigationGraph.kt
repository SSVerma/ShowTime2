package com.ssverma.showtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.ImageShotsListScreen
import com.ssverma.showtime.ui.home.HomeViewModel
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.*
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen


@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: StandaloneDestination = AppDestination.Home
) {

    NavHost(
        navController = navController,
        startDestination = startDestination.placeholderRouteString(),
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

        navigation(
            graphDestination = AppDestination.MovieDetailsGraph,
            startDestination = AppDestination.MovieDetails
        ) {

            composable<MovieDetailsViewModel>(
                graphDestination = AppDestination.MovieDetailsGraph,
                destination = AppDestination.MovieDetails,
                navController = navController
            ) {

                MovieDetailsScreen(
                    viewModel = it.graphScopedViewModel,
                    onBackPressed = { navController.popBackStack() },
                    openMovieDetails = { movieId ->
                        navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                    },
                    openImageShotsList = {
                        navController.navigateTo(AppDestination.ImageShots.actualRoute)
                    },
                    openImageShot = {
                        navController.navigateTo(AppDestination.ImagePager.actualRoute(it))
                    }
                )
            }

            composable<MovieDetailsViewModel>(
                graphDestination = AppDestination.MovieDetailsGraph,
                destination = AppDestination.ImageShots,
                navController = navController
            ) {

                ImageShotsListScreen(
                    liveImageShots = it.graphScopedViewModel.imageShots,
                    onBackPressed = { navController.popBackStack() },
                    openImagePager = {
                        navController.navigateTo(AppDestination.ImagePager.actualRoute(it))
                    }
                )
            }

            composable<MovieDetailsViewModel>(
                graphDestination = AppDestination.MovieDetailsGraph,
                destination = AppDestination.ImagePager,
                navController = navController
            ) {

                ImagePagerScreen(
                    liveImageShots = it.graphScopedViewModel.imageShots,
                    defaultPageIndex = it.navBackStackEntry.arguments?.getInt(AppDestination.ImagePager.PageIndex)
                        ?: 0,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRouteString(),
        arguments = destination.arguments(),
        content = content
    )
}

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
        else -> identifier
    }
}

private fun Destination.arguments(): List<NamedNavArgument> {
    return if (this is DependentDestination<*>) arguments() else emptyList()
}

class NavGraphElement<T : ViewModel>(
    val graphScopedViewModel: T,
    val navBackStackEntry: NavBackStackEntry
)