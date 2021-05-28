package com.ssverma.showtime.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.ssverma.showtime.AppActions
import com.ssverma.showtime.AppDestinations
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.common.AppIcons
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.MovieScreen
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen

sealed class BottomNavScreen(
    val route: String,
    @StringRes val tabTitleStringRes: Int,
    val tabIcon: ImageVector
) {
    object Home : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.MovieRoute,
        tabTitleStringRes = R.string.movie,
        tabIcon = AppIcons.Home
    )

    object Explore : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.TvShowRoute,
        tabTitleStringRes = R.string.tv_show,
        tabIcon = AppIcons.Face
    )

    object People : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.PeopleRoute,
        tabTitleStringRes = R.string.people,
        tabIcon = AppIcons.Person
    )

    object Library : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.LibraryRoute,
        tabTitleStringRes = R.string.library,
        tabIcon = AppIcons.Star
    )
}

private val bottomTabs = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.Explore,
    BottomNavScreen.People,
    BottomNavScreen.Library,
)

@Composable
fun HomePage(viewModel: HomeViewModel, actions: AppActions) {
    val bottomNavController = rememberNavController()

    Scaffold(
        backgroundColor = MaterialTheme.colors.surface,
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.navigationBarsHeight(additional = 56.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                bottomTabs.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                imageVector = screen.tabIcon,
                                contentDescription = stringResource(id = screen.tabTitleStringRes)
                            )
                        },
                        label = { Text(text = stringResource(id = screen.tabTitleStringRes)) },
                        selected = currentRoute == screen.route,
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = LocalContentColor.current,
                        modifier = Modifier.navigationBarsPadding(),
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(bottomNavController.graph.startDestinationRoute!!) {
                                    saveState = true
                                }
                            }
                        }
                    )
                }
            }
        }

    ) {
        NavHost(
            navController = bottomNavController,
            startDestination = AppDestinations.BottomNavDestinations.MovieRoute
        ) {
            composable(AppDestinations.BottomNavDestinations.MovieRoute) {
                MovieScreen(
                    viewModel = viewModel,
                    onNavigateToMovieList = { type ->
                        actions.navigateToMovieList(type)
                    }
                )
            }
            composable(AppDestinations.BottomNavDestinations.TvShowRoute) {
                TvShowScreen()
            }
            composable(AppDestinations.BottomNavDestinations.PeopleRoute) {
                PeopleScreen()
            }
            composable(AppDestinations.BottomNavDestinations.LibraryRoute) {
                LibraryScreen()
            }
        }
    }
}

@Composable
fun HomePageAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        modifier = modifier.height(56.dp),
        contentColor = Color.White //Always blackish tint on background
    ) {
        Image(
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { /* todo */ }
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { /* todo */ }
        ) {
            Icon(
                imageVector = AppIcons.AccountCircle,
                contentDescription = stringResource(R.string.account)
            )
        }
    }
}