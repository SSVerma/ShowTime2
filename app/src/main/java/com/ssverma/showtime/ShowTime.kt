package com.ssverma.showtime

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.showtime.navigation.ShowTimeNavHost
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShowTime() {
    ShowTimeTheme {
        val navController = rememberAnimatedNavController()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            bottomBar = {
                ShowTimeBottomBar(
                    currentNavDestination = currentDestination,
                    onTopLevelNavItemSelected = { navItem ->
                        selectTopLevelNavItem(
                            navItem = navItem,
                            navController = navController
                        )
                    }
                )
            }
        ) { innerPaddingModifier ->
            ShowTimeNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPaddingModifier)
            )
        }
    }
}

private fun selectTopLevelNavItem(
    navItem: ShowTimeTopLevelNavItem,
    navController: NavController
) {
    navController.navigate(navItem.destination.placeholderRoute.asNavRoute()) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun showBottomBar(
    currentNavDestination: NavDestination?,
    bottomNavDestinations: List<ShowTimeTopLevelNavItem>
): Boolean {
    val routes = bottomNavDestinations.map { it.destination.placeholderRoute.asNavRoute() }
    return routes.contains(currentNavDestination?.route)
}

@Composable
fun ShowTimeBottomBar(
    currentNavDestination: NavDestination?,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!showBottomBar(currentNavDestination, bottomNavItems)) {
        return
    }

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        elevation = 16.dp,
    ) {
        bottomNavItems.forEach { navItem ->
            val selected = currentNavDestination
                ?.hierarchy
                ?.any { it.route == navItem.destination.placeholderRoute.asNavRoute() } == true

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.iconResId),
                        contentDescription = stringResource(id = navItem.titleResId)
                    )
                },
                label = { Text(text = stringResource(id = navItem.titleResId)) },
                selected = selected,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onBackground.copy(alpha = 0.87f),
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    onTopLevelNavItemSelected(navItem)
                }
            )
        }
    }
}
