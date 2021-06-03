package com.ssverma.showtime

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.ssverma.showtime.navigation.ShowTimeNavHost
import com.ssverma.showtime.ui.home.HomeBottomNavScreen
import com.ssverma.showtime.ui.home.homeBottomNavScreens
import com.ssverma.showtime.ui.theme.ShowTimeTheme

@Composable
fun ShowTime() {
    ProvideWindowInsets {
        ShowTimeTheme {
            val bottomNavScreens = remember { homeBottomNavScreens }
            val navController = rememberNavController()

            Scaffold(
                bottomBar = {
                    ShowTimeBottomBar(navController = navController, bottomNavScreens)
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
    bottomNavScreens: List<HomeBottomNavScreen>
): Boolean {
    val routes = bottomNavScreens.map { it.route }
    return routes.contains(backStackEntry?.destination?.route)
}

@Composable
fun ShowTimeBottomBar(
    navController: NavHostController,
    bottomNavScreens: List<HomeBottomNavScreen>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
        ?: HomeBottomNavScreen.Movie.route

    if (!showBottomBar(navBackStackEntry, bottomNavScreens)) {
        return
    }

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = Modifier.navigationBarsHeight(additional = 56.dp)
    ) {
        bottomNavScreens.forEach { screen ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = screen.tabIcon,
                        contentDescription = stringResource(id = screen.tabTitleRes)
                    )
                },
                label = { Text(text = stringResource(id = screen.tabTitleRes)) },
                selected = currentRoute == screen.route,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = LocalContentColor.current,
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
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
