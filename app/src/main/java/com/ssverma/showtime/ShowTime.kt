package com.ssverma.showtime

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppInfoTrigger
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.showtime.navigation.ShowTimeNavHost
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShowTime() {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState(initial = AppTheme.System)
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)

    ShowTimeTheme(
        appTheme = appTheme,
        dynamicColor = isDynamicColorEnabled
    ) {
        val darkTheme = when (appTheme) {
            AppTheme.System -> isSystemInDarkTheme()
            AppTheme.Light -> false
            AppTheme.Dark -> true
        }

        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as android.app.Activity).window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }

        val navController = rememberNavController()

        val isAppInfoDismissed by appStateHolder.isAppInfoDismissed.collectAsState()
        var manuallyDismissedThisSession by remember { mutableStateOf(false) }
        var showManualAppInfoSheet by remember { mutableStateOf(false) }

        if ((!isAppInfoDismissed && !manuallyDismissedThisSession) || showManualAppInfoSheet) {
            AppInfoBottomSheet(
                showDontShowAgain = !showManualAppInfoSheet,
                onDismissRequest = { dontShowAgain ->
                    if (!showManualAppInfoSheet) {
                        manuallyDismissedThisSession = true
                        appStateHolder.onDismissAppInfo(dontShowAgain)
                    }
                    showManualAppInfoSheet = false
                }
            )
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        CompositionLocalProvider(
            LocalAppInfoTrigger provides { showManualAppInfoSheet = true }
        ) {
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
                    modifier = Modifier.padding(
                        start = innerPaddingModifier.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPaddingModifier.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = innerPaddingModifier.calculateBottomPadding()
                    )
                )
            }
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

    NavigationBar(
        modifier = modifier,
    ) {
        bottomNavItems.forEach { navItem ->
            val selected = currentNavDestination
                ?.hierarchy
                ?.any { it.route == navItem.destination.placeholderRoute.asNavRoute() } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = navItem.iconResId),
                        contentDescription = stringResource(id = navItem.titleResId)
                    )
                },
                label = { Text(text = stringResource(id = navItem.titleResId)) },
                selected = selected,
                modifier = Modifier.navigationBarsPadding(),
                onClick = {
                    onTopLevelNavItemSelected(navItem)
                }
            )
        }
    }
}
