package com.ssverma.showtime

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.core.navigation.navigateTo
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppInfoTrigger
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.showtime.navigation.ShowTimeNavHost
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems
import com.ssverma.showtime.notifications.NotificationPermissionHandler

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
        val showAppInfoSheet =
            (!isAppInfoDismissed && !manuallyDismissedThisSession) || showManualAppInfoSheet

        if (showAppInfoSheet) {
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
            val notificationManager = LocalNotificationManager.current
            val isHomePage = showBottomBar(currentDestination, ShowTimeTopLevelNavItems)

            NotificationPermissionHandler(
                notificationManager = notificationManager,
                canRequest = isHomePage && !showAppInfoSheet
            )

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPaddingModifier ->
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ShowTimeNavHost(
                        navController = navController,
                        modifier = Modifier.padding(
                            start = innerPaddingModifier.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPaddingModifier.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = 0.dp
                        )
                    )

                    ShowTimeBottomBar(
                        currentNavDestination = currentDestination,
                        onTopLevelNavItemSelected = { navItem ->
                            selectTopLevelNavItem(
                                navItem = navItem,
                                navController = navController
                            )
                        },
                        onSearchPressed = {
                            navController.navigateTo(SearchDestination.actualRoute)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
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
fun ShowTimeToolbarTab(
    iconResId: Int,
    titleResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)

    val animatedColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "TabColor"
    )

    val capsuleColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        label = "CapsuleColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(capsuleColor)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .animateContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = stringResource(id = titleResId),
                        tint = animatedColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = stringResource(id = titleResId),
                    color = animatedColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowTimeBottomBar(
    currentNavDestination: NavDestination?,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    onSearchPressed: () -> Unit,
    modifier: Modifier = Modifier,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!showBottomBar(currentNavDestination, bottomNavItems)) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier
                .width(276.dp)
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
            ),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Movie
            val movieItem = bottomNavItems[0]
            val movieSelected = currentNavDestination
                ?.hierarchy
                ?.any { it.route == movieItem.destination.placeholderRoute.asNavRoute() } == true
            ShowTimeToolbarTab(
                iconResId = movieItem.iconResId,
                titleResId = movieItem.titleResId,
                selected = movieSelected,
                onClick = { onTopLevelNavItemSelected(movieItem) },
                modifier = Modifier.weight(1f)
            )

            // Tv
            val tvItem = bottomNavItems[1]
            val tvSelected = currentNavDestination
                ?.hierarchy
                ?.any { it.route == tvItem.destination.placeholderRoute.asNavRoute() } == true
            ShowTimeToolbarTab(
                iconResId = tvItem.iconResId,
                titleResId = tvItem.titleResId,
                selected = tvSelected,
                onClick = { onTopLevelNavItemSelected(tvItem) },
                modifier = Modifier.weight(1f)
            )

            // Person
            val personItem = bottomNavItems[2]
            val personSelected = currentNavDestination
                ?.hierarchy
                ?.any { it.route == personItem.destination.placeholderRoute.asNavRoute() } == true
            ShowTimeToolbarTab(
                iconResId = personItem.iconResId,
                titleResId = personItem.titleResId,
                selected = personSelected,
                onClick = { onTopLevelNavItemSelected(personItem) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Search Icon FAB (Outside the toolbar, matching Photos app style)
        Surface(
            onClick = onSearchPressed,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
