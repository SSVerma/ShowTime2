package com.ssverma.showtime

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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation3.runtime.NavKey
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.rememberNavigationState
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.feature.movie.navigation.MovieHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppInfoTrigger
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.showtime.navigation.ShowTimeNavDisplay
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems
import com.ssverma.showtime.notifications.NotificationPermissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTime(
    initialDeepLinkKey: NavKey? = null
) {
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

        val topLevelRoutes = remember {
            ShowTimeTopLevelNavItems.map { it.navKey }.toSet()
        }

        val navigationState = rememberNavigationState(
            startRoute = MovieHomeNavKey,
            topLevelRoutes = topLevelRoutes
        )

        val navigator = remember { Navigator(navigationState) }

        LaunchedEffect(initialDeepLinkKey) {
            initialDeepLinkKey?.let { navigator.navigate(it) }
        }

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

        val currentDestination =
            navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

        CompositionLocalProvider(
            LocalAppInfoTrigger provides { showManualAppInfoSheet = true }
        ) {
            val notificationManager = LocalNotificationManager.current
            val isHomePage = isHomePage(
                currentNavKey = currentDestination,
                bottomNavDestinations = ShowTimeTopLevelNavItems
            )

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
                    ShowTimeNavDisplay(
                        navigationState = navigationState,
                        navigator = navigator,
                        openLibraryPage = {
                            navigator.navigate(ShowTimeTopLevelNavItem.Library.navKey)
                        },
                        modifier = Modifier.padding(
                            start = innerPaddingModifier.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPaddingModifier.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = 0.dp
                        )
                    )

                    ShowTimeBottomBar(
                        currentNavKey = currentDestination,
                        topLevelNavKey = navigationState.topLevelRoute,
                        onTopLevelNavItemSelected = { navItem ->
                            navigator.navigate(navItem.navKey)
                        },
                        onSearchPressed = {
                            navigator.navigate(SearchNavKey)
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
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

private fun isHomePage(
    currentNavKey: NavKey?,
    bottomNavDestinations: List<ShowTimeTopLevelNavItem>
): Boolean {
    return bottomNavDestinations.any { it.navKey == currentNavKey }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowTimeBottomBar(
    currentNavKey: NavKey?,
    topLevelNavKey: NavKey,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    onSearchPressed: () -> Unit,
    modifier: Modifier = Modifier,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!isHomePage(currentNavKey, bottomNavItems)) {
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
            bottomNavItems.forEach { item ->
                ShowTimeToolbarTab(
                    iconResId = item.iconResId,
                    titleResId = item.titleResId,
                    selected = item.navKey == topLevelNavKey,
                    onClick = { onTopLevelNavItemSelected(item) },
                    modifier = Modifier.weight(1f)
                )
            }
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
