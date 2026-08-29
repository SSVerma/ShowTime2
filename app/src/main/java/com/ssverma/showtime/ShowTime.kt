package com.ssverma.showtime

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.navigation3.runtime.NavKey
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
import com.ssverma.core.navigation.nav3.LocalNavAnimatedVisibilityScope
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.rememberNavigationState
import com.ssverma.core.notifications.LocalNotificationManager
import com.ssverma.core.ui.layout.LocalFloatingBarsVisible
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.feature.account.navigation.BackupSyncNavKey
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.account.navigation.TraktSyncNavKey
import com.ssverma.feature.account.ui.pro.ProPaywallBottomSheet
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.ui.LocalAppInfoTrigger
import com.ssverma.shared.ui.LocalAppStateHolder
import com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet
import com.ssverma.showtime.component.ShowTimeDrawerContent
import com.ssverma.showtime.component.ShowTimeTopSearchBar
import com.ssverma.showtime.navigation.DashboardHomeNavKey
import com.ssverma.showtime.navigation.ShowTimeNavDisplay
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems
import com.ssverma.showtime.notifications.NotificationPermissionHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ShowTime(
    initialDeepLinkKey: NavKey? = null
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState(initial = AppTheme.System)
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)
    val isProActive by appStateHolder.isProActive.collectAsState(initial = false)
    val googleUser by appStateHolder.googleUser.collectAsState(initial = null)

    ShowTimeTheme(
        appTheme = appTheme,
        dynamicColor = isDynamicColorEnabled
    ) {
        val darkTheme = when (appTheme) {
            AppTheme.System -> isSystemInDarkTheme()
            AppTheme.Light -> false
            AppTheme.Dark,
            AppTheme.OledMidnight -> true
        }

        val view = LocalView.current
        val context = LocalContext.current
        if (!view.isInEditMode) {
            LaunchedEffect(darkTheme) {
                val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
            }
        }

        val topLevelRoutes = remember {
            ShowTimeTopLevelNavItems.map { it.navKey }.toSet()
        }

        val navigationState = rememberNavigationState(
            startRoute = DashboardHomeNavKey,
            topLevelRoutes = topLevelRoutes
        )

        val navigator = remember(navigationState) { Navigator(navigationState) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(initialDeepLinkKey) {
            initialDeepLinkKey?.let { navigator.navigate(it) }
        }

        val isAppInfoDismissed by appStateHolder.isAppInfoDismissed.collectAsState()
        var manuallyDismissedThisSession by remember { mutableStateOf(false) }
        var showManualAppInfoSheet by remember { mutableStateOf(false) }
        var showThemeSelectionSheet by remember { mutableStateOf(false) }
        var showLocalizationSettingsSheet by remember { mutableStateOf(false) }
        var showProPaywallSheet by remember { mutableStateOf(false) }

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

        if (showThemeSelectionSheet) {
            ThemeSelectionBottomSheet(
                currentTheme = appTheme,
                isDynamicColorEnabled = isDynamicColorEnabled,
                isProActive = isProActive,
                onThemeSelected = { newTheme ->
                    appStateHolder.updateAppTheme(newTheme)
                },
                onDynamicColorToggled = { enabled ->
                    appStateHolder.updateDynamicColor(enabled)
                },
                onUpgradeToPro = {
                    showThemeSelectionSheet = false
                    showProPaywallSheet = true
                },
                onDismissRequest = {
                    showThemeSelectionSheet = false
                }
            )
        }

        if (showLocalizationSettingsSheet) {
            LocalizationSettingsBottomSheet(
                onDismissRequest = {
                    showLocalizationSettingsSheet = false
                }
            )
        }

        if (showProPaywallSheet) {
            val availableProducts by appStateHolder.availableProducts.collectAsState()
            var isRestoring by remember { mutableStateOf(false) }

            ProPaywallBottomSheet(
                products = availableProducts,
                isProActive = isProActive,
                isRestoring = isRestoring,
                onPurchaseClick = { act, product ->
                    appStateHolder.purchaseProduct(activity = act, product = product)
                },
                onRestoreClick = {
                    coroutineScope.launch {
                        isRestoring = true
                        appStateHolder.restorePurchases()
                        isRestoring = false
                    }
                },
                onDismissRequest = {
                    showProPaywallSheet = false
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

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = isHomePage,
                drawerContent = {
                    ShowTimeDrawerContent(
                        onOpenPeople = {
                            coroutineScope.launch { drawerState.close() }
                            navigator.navigate(PersonHomeNavKey)
                        },
                        onOpenCinemaGame = {
                            coroutineScope.launch { drawerState.close() }
                            navigator.navigate(CinemaGameNavKey)
                        },
                        onOpenReceipt = {
                            coroutineScope.launch { drawerState.close() }
                            navigator.navigate(CinemaReceiptNavKey)
                        },
                        onOpenBackup = {
                            coroutineScope.launch { drawerState.close() }
                            navigator.navigate(BackupSyncNavKey)
                        },
                        onOpenTrakt = {
                            coroutineScope.launch { drawerState.close() }
                            navigator.navigate(TraktSyncNavKey)
                        },
                        onOpenPro = {
                            coroutineScope.launch { drawerState.close() }
                            showProPaywallSheet = true
                        },
                        onOpenTheme = {
                            coroutineScope.launch { drawerState.close() }
                            showThemeSelectionSheet = true
                        },
                        onOpenLocalization = {
                            coroutineScope.launch { drawerState.close() }
                            showLocalizationSettingsSheet = true
                        },
                        onOpenPrivacy = {
                            coroutineScope.launch { drawerState.close() }
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://github.com/SSVerma/ShowTime/blob/main/PRIVACY_POLICY.md".toUri()
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                showManualAppInfoSheet = true
                            }
                        },
                        onOpenLicenses = {
                            coroutineScope.launch { drawerState.close() }
                            showManualAppInfoSheet = true
                        },
                        onOpenAbout = {
                            coroutineScope.launch { drawerState.close() }
                            showManualAppInfoSheet = true
                        }
                    )
                }
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPaddingModifier ->
                    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

                    LaunchedEffect(currentDestination) {
                        isBottomBarVisible = true
                    }

                    val bottomBarNestedScrollConnection = remember {
                        object : NestedScrollConnection {
                            private var accumulatedScroll = 0f

                            override fun onPreScroll(
                                available: Offset,
                                source: NestedScrollSource
                            ): Offset {
                                accumulatedScroll += available.y
                                if (accumulatedScroll < -40f) {
                                    if (isBottomBarVisible) {
                                        isBottomBarVisible = false
                                    }
                                    accumulatedScroll = 0f
                                } else if (accumulatedScroll > 40f) {
                                    if (!isBottomBarVisible) {
                                        isBottomBarVisible = true
                                    }
                                    accumulatedScroll = 0f
                                }
                                return Offset.Zero
                            }
                        }
                    }

                    SharedTransitionLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(bottomBarNestedScrollConnection)
                    ) {
                        CompositionLocalProvider(
                            LocalFloatingBarsVisible provides isBottomBarVisible,
                            LocalSharedTransitionScope provides this
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                ShowTimeNavDisplay(
                                    navigationState = navigationState,
                                    navigator = navigator,
                                    openLibraryPage = { navKey ->
                                        navigator.navigate(navKey)
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = innerPaddingModifier.calculateStartPadding(
                                                LayoutDirection.Ltr
                                            ),
                                            end = innerPaddingModifier.calculateEndPadding(
                                                LayoutDirection.Ltr
                                            )
                                        )
                                )

                                AnimatedVisibility(
                                    visible = isHomePage && isBottomBarVisible,
                                    enter = if (isHomePage) {
                                        slideInVertically(
                                            initialOffsetY = { -it },
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) + fadeIn(animationSpec = tween(220))
                                    } else {
                                        fadeIn(
                                            animationSpec = tween(
                                                280,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    },
                                    exit = if (isHomePage) {
                                        slideOutVertically(
                                            targetOffsetY = { -it },
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) + fadeOut(animationSpec = tween(220))
                                    } else {
                                        fadeOut(
                                            animationSpec = tween(
                                                280,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .statusBarsPadding()
                                ) {
                                    CompositionLocalProvider(
                                        LocalNavAnimatedVisibilityScope provides this
                                    ) {
                                        ShowTimeTopSearchBar(
                                            googleUser = googleUser,
                                            isProActive = isProActive,
                                            onMenuClick = {
                                                coroutineScope.launch { drawerState.open() }
                                            },
                                            onSearchClick = {
                                                navigator.navigate(SearchNavKey)
                                            },
                                            onProfileClick = {
                                                navigator.navigate(ProfileNavKey)
                                            }
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isHomePage && isBottomBarVisible,
                                    enter = if (isHomePage) {
                                        slideInVertically(
                                            initialOffsetY = { it },
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) + fadeIn(animationSpec = tween(220))
                                    } else {
                                        fadeIn(
                                            animationSpec = tween(
                                                280,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    },
                                    exit = if (isHomePage) {
                                        slideOutVertically(
                                            targetOffsetY = { it },
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ) + fadeOut(animationSpec = tween(220))
                                    } else {
                                        fadeOut(
                                            animationSpec = tween(
                                                280,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    ShowTimeBottomBar(
                                        currentNavKey = currentDestination,
                                        topLevelNavKey = navigationState.topLevelRoute,
                                        onTopLevelNavItemSelected = { navItem ->
                                            navigator.navigate(navItem.navKey)
                                        }
                                    )
                                }
                            }
                        }
                    }
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
    val haptic = LocalHapticFeedback.current
    val activeContentColor = MaterialTheme.colorScheme.primary
    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    val animatedColor by animateColorAsState(
        targetValue = if (selected) activeContentColor else inactiveContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "TabColor"
    )

    val capsuleColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "CapsuleColor"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(CircleShape)
            .background(capsuleColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = titleResId),
                tint = animatedColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = titleResId),
                color = animatedColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private fun isHomePage(
    currentNavKey: NavKey?,
    bottomNavDestinations: List<ShowTimeTopLevelNavItem>
): Boolean {
    return bottomNavDestinations.any {
        it.navKey == currentNavKey || (currentNavKey != null && it.navKey::class == currentNavKey::class)
    }
}

@Composable
fun ShowTimeBottomBar(
    currentNavKey: NavKey?,
    topLevelNavKey: NavKey,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!isHomePage(currentNavKey, bottomNavItems)) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                clip = false
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    ShowTimeToolbarTab(
                        iconResId = item.iconResId,
                        titleResId = item.titleResId,
                        selected = item.navKey == topLevelNavKey || item.navKey::class == topLevelNavKey::class,
                        onClick = { onTopLevelNavItemSelected(item) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
