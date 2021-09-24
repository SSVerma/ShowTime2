package com.ssverma.showtime

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ssverma.showtime.navigation.ShowTimeNavHost
import com.ssverma.showtime.navigation.navigateTo
import com.ssverma.showtime.ui.home.HomeBottomNavItem
import com.ssverma.showtime.ui.home.homeBottomNavItems
import com.ssverma.showtime.ui.theme.ShowTimeTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTime() {
    ProvideWindowInsets {
        ShowTimeTheme {
            val bottomNavItems = remember { homeBottomNavItems }
            val navController = rememberAnimatedNavController()

            Scaffold(
                bottomBar = {
                    ShowTimeBottomBar(navController = navController, bottomNavItems)
                }
            ) { innerPaddingModifier ->
                ShowTimeNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPaddingModifier)
                )
            }
        }
    }
}

private fun showBottomBar(
    backStackEntry: NavBackStackEntry?,
    bottomNavScreens: List<HomeBottomNavItem>
): Boolean {
    val routes = bottomNavScreens.map { it.linkedDestination.placeholderRoute.asRoutableString() }
    return routes.contains(backStackEntry?.destination?.route)
}

@Composable
fun ShowTimeBottomBar(
    navController: NavHostController,
    bottomNavItems: List<HomeBottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    if (!showBottomBar(navBackStackEntry, bottomNavItems)) {
        return
    }

    val currentPlaceholderRoute = navBackStackEntry?.destination?.route
        ?: HomeBottomNavItem.Movie.linkedDestination.placeholderRoute.asRoutableString()

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.navigationBarsHeight(additional = 56.dp),
        elevation = 16.dp
    ) {
        bottomNavItems.forEach { navItem ->
            val navItemDestinationRoute =
                navItem.linkedDestination.placeholderRoute.asRoutableString()

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.tabIconRes),
                        contentDescription = stringResource(id = navItem.tabTitleRes)
                    )
                },
                label = { Text(text = stringResource(id = navItem.tabTitleRes)) },
                selected = currentPlaceholderRoute == navItemDestinationRoute,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.87f),
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    if (navItemDestinationRoute != currentPlaceholderRoute) {
                        navController.navigateTo(navItem.linkedDestination.actualRoute) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(findStartDestination(navController.graph).id) {
                                saveState = true
                            }
                        }
                    }
                }
            )
        }
    }
}

private val NavGraph.startDestination: NavDestination?
    get() = findNode(startDestinationId)

private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
    return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
}
