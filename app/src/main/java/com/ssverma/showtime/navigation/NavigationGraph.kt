package com.ssverma.showtime.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.ImageShotsListScreen
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.*
import com.ssverma.showtime.ui.people.PersonDetailsScreen
import com.ssverma.showtime.ui.people.PersonImageShotsScreen
import com.ssverma.showtime.ui.people.PersonScreen
import com.ssverma.showtime.ui.tv.*


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTimeNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberAnimatedNavController(),
    startDestination: StandaloneDestination = AppDestination.Home
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

        navigation(
            graphDestination = AppDestination.Home,
            startDestination = AppDestination.HomeBottomNavDestination.Movie,
        ) {
            composable(
                destination = AppDestination.HomeBottomNavDestination.Movie
            ) {
                MovieScreen(
                    viewModel = hiltViewModel(),
                    openMovieList = { listingArgs ->
                        navController.navigateTo(AppDestination.MovieList.actualRoute(listingArgs))
                    },
                    openMovieDetails = { movieId ->
                        navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                    }
                )
            }

            composable(
                destination = AppDestination.HomeBottomNavDestination.Tv
            ) {
                TvShowScreen(
                    viewModel = hiltViewModel(),
                    openTvShowDetails = { tvShowId ->
                        navController.navigateTo(AppDestination.TvShowDetails.actualRoute(tvShowId))
                    },
                    openTvShowList = { listingArgs ->
                        navController.navigateTo(
                            AppDestination.TvShowList.actualRoute(listingArgs)
                        )
                    }
                )
            }

            composable(
                destination = AppDestination.HomeBottomNavDestination.People
            ) {
                PersonScreen(
                    viewModel = hiltViewModel(),
                    openPersonDetailsScreen = { personId ->
                        navController.navigateTo(AppDestination.PersonDetails.actualRoute(personId))
                    },
                    openMovieDetailsScreen = { movieId ->
                        navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                    },
                    openTvShowDetailsScreen = { tvShowId ->
                        navController.navigateTo(AppDestination.TvShowDetails.actualRoute(tvShowId))
                    }
                )
            }

            composable(
                destination = AppDestination.HomeBottomNavDestination.Library
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
                    navController.navigateTo(AppDestination.MovieImageShots.actualRoute)
                },
                openImageShot = { index ->
                    navController.navigateTo(AppDestination.MovieImagePager.actualRoute(index))
                },
                openReviewsList = { movieId ->
                    navController.navigateTo(AppDestination.MovieReviews.actualRoute(movieId))
                },
                openPersonDetails = { personId ->
                    navController.navigateTo(AppDestination.PersonDetails.actualRoute(personId))
                },
                openMovieList = { listingArgs ->
                    navController.navigateTo(
                        AppDestination.MovieList.actualRoute(listingArgs)
                    )
                }
            )
        }

        composable(AppDestination.MovieImageShots) {
            val movieDetailsViewModel: MovieDetailsViewModel =
                navController.destinationViewModel(destination = AppDestination.MovieDetails)

            ImageShotsListScreen(
                observableImageShots = movieDetailsViewModel.imageShots,
                onBackPressed = { navController.popBackStack() },
                openImagePager = {
                    navController.navigateTo(AppDestination.MovieImagePager.actualRoute(it))
                }
            )
        }

        composable(AppDestination.MovieImagePager) {
            val movieDetailsViewModel = navController
                .destinationViewModel<MovieDetailsViewModel>(destination = AppDestination.MovieDetails)

            ImagePagerScreen(
                observableImageShots = movieDetailsViewModel.imageShots,
                defaultPageIndex = it.arguments?.getInt(AppDestination.MovieImagePager.PageIndex)
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

        composable(destination = AppDestination.PersonDetails) {
            PersonDetailsScreen(
                viewModel = hiltViewModel(it),
                onBackPress = { navController.popBackStack() },
                openMovieDetails = { movieId ->
                    navController.navigateTo(AppDestination.MovieDetails.actualRoute(movieId))
                },
                openTvShowDetails = { tvShowId ->
                    navController.navigateTo(AppDestination.TvShowDetails.actualRoute(tvShowId))
                },
                openPersonAllImages = { personId ->
                    navController.navigateTo(AppDestination.PersonImages.actualRoute(personId))
                }
            )
        }

        composable(destination = AppDestination.PersonImages) {
            PersonImageShotsScreen(
                viewModel = hiltViewModel(it),
                onBackPressed = { navController.popBackStack() },
            )
        }

        composable(destination = AppDestination.TvShowDetails) {
            TvShowDetailsScreen(
                viewModel = hiltViewModel(it),
                onBackPressed = { navController.popBackStack() },
                openTvShowDetails = { tvShowId ->
                    navController.navigateTo(AppDestination.TvShowDetails.actualRoute(tvShowId))
                },
                openImageShotsList = {
                    navController.navigateTo(AppDestination.TvShowImageShots.actualRoute)
                },
                openImageShot = { index ->
                    navController.navigateTo(AppDestination.TvImagePager.actualRoute(index))
                },
                openReviewsList = { tvShowId ->
                    navController.navigateTo(AppDestination.TvShowReviews.actualRoute(tvShowId))
                },
                openPersonDetails = { personId ->
                    navController.navigateTo(AppDestination.PersonDetails.actualRoute(personId))
                },
                openTvShowList = { listingArgs ->
                    navController.navigateTo(
                        AppDestination.TvShowList.actualRoute(listingArgs)
                    )
                },
                openTvSeasonDetails = { seasonLaunchable ->
                    navController.navigateTo(
                        AppDestination.TvSeasonDetails.actualRoute(
                            seasonLaunchable
                        )
                    )
                }
            )
        }

        composable(AppDestination.TvShowImageShots) {
            val tvShowDetailsViewModel: TvShowDetailsViewModel =
                navController.destinationViewModel(destination = AppDestination.TvShowDetails)

            ImageShotsListScreen(
                observableImageShots = tvShowDetailsViewModel.imageShots,
                onBackPressed = { navController.popBackStack() },
                openImagePager = {
                    navController.navigateTo(AppDestination.TvImagePager.actualRoute(it))
                }
            )
        }

        composable(destination = AppDestination.TvShowReviews) {
            TvShowReviewsScreen(
                viewModel = hiltViewModel(it),
                onBackPress = { navController.popBackStack() }
            )
        }

        composable(AppDestination.TvImagePager) {
            val tvShowDetailsViewModel = navController
                .destinationViewModel<TvShowDetailsViewModel>(destination = AppDestination.TvShowDetails)

            ImagePagerScreen(
                observableImageShots = tvShowDetailsViewModel.imageShots,
                defaultPageIndex = it.arguments?.getInt(AppDestination.TvImagePager.PageIndex)
                    ?: 0,
                onBackPressed = { navController.popBackStack() }
            )
        }

        composable(destination = AppDestination.TvSeasonDetails) {
            TvSeasonDetailsScreen(
                viewModel = hiltViewModel(it),
                onBackPress = { navController.popBackStack() },
                openEpisodeDetails = { episodeLaunchable ->
                    navController.navigateTo(
                        AppDestination.TvEpisodeDetails.actualRoute(episodeLaunchable)
                    )
                },
                openPersonDetails = { personId ->
                    navController.navigateTo(AppDestination.PersonDetails.actualRoute(personId))
                }
            )
        }

        composable(destination = AppDestination.TvEpisodeDetails) {
            TvEpisodeDetailsScreen(
                viewModel = hiltViewModel(it),
                onBackPress = { navController.popBackStack() },
                openPersonDetails = { personId ->
                    navController.navigateTo(AppDestination.PersonDetails.actualRoute(personId))
                }
            )
        }

        composable(AppDestination.TvShowList) {
            TvShowListScreen(
                viewModel = hiltViewModel(it),
                onBackPressed = { navController.popBackStack() },
                openTvShowDetails = { tvShowId ->
                    navController.navigateTo(AppDestination.TvShowDetails.actualRoute(tvShowId))
                }
            )
        }
    }
}

@Composable
private inline fun <reified VM : ViewModel> NavController.destinationViewModel(destination: Destination): VM {
    return hiltViewModel(
        remember { getBackStackEntry(destination.placeholderRoute.asNavRoute()) }
    )
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.composable(
    destination: Destination,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = destination.placeholderRoute.asNavRoute(),
        arguments = destination.arguments,
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
private inline fun <reified VM : ViewModel> NavGraphBuilder.composable(
    graphDestination: GraphDestination,
    destination: Destination,
    navController: NavHostController,
    crossinline content: @Composable (navGraphElement: NavGraphElement<VM>) -> Unit
) {
    composable(
        route = destination.placeholderRoute.asNavRoute(),
        arguments = destination.arguments,
    ) {
        val viewModel = navController.destinationViewModel<VM>(destination = graphDestination)
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
    enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        route = graphDestination.placeholderRoute.asNavRoute(),
        startDestination = startDestination.placeholderRoute.asNavRoute(),
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        builder = builder
    )
}

fun NavController.navigateTo(route: ActualRoute) {
    navigate(route = route.asNavRoute())
}

fun NavController.navigateTo(route: ActualRoute, builder: NavOptionsBuilder.() -> Unit) {
    navigate(
        route = route.asNavRoute(),
        builder = builder
    )
}

data class NavGraphElement<T : ViewModel>(
    val graphScopedViewModel: T,
    val navBackStackEntry: NavBackStackEntry
)