package com.ssverma.showtime.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.navigation.nav3.LocalNavAnimatedVisibilityScope
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.navigation.nav3.NavigationState
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.NavigationChromeType
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.layout.LocalFloatingBarsVisible
import com.ssverma.core.ui.layout.rememberWindowTopBarInsets
import com.ssverma.core.ui.layout.windowTopBarInsetsPadding
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.showtime.navigation.ShowTimeNavDisplay

/**
 * Single unified adaptive application shell hosting navigation chrome for Compact, Medium, and Large
 * viewports while keeping [ShowTimeNavDisplay] mounted in one stable slot across window resizes.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ShowTimeAdaptiveShell(
    drawerState: DrawerState,
    navigationState: NavigationState,
    navigator: Navigator,
    currentDestination: NavKey?,
    isHomePage: Boolean,
    googleUser: GoogleUser?,
    isProActive: Boolean,
    watchProviderRegion: String,
    preferredOriginalLanguage: String,
    snackbarHostState: SnackbarHostState,
    menuActions: ShowTimeMenuActions,
    onOpenDrawer: () -> Unit,
    onOpenLocalization: () -> Unit,
    onResetLanguageFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chromeType = LocalAdaptiveLayoutInfo.current.navigationChromeType
    val isFloatingChrome = chromeType == NavigationChromeType.FloatingBottomBar
    val topBarInsets = rememberWindowTopBarInsets()

    LaunchedEffect(chromeType) {
        if (chromeType == NavigationChromeType.PermanentSidebar && drawerState.isOpen) {
            drawerState.close()
        }
    }

    var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(currentDestination, isFloatingChrome) {
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isFloatingChrome && isHomePage,
        drawerContent = {
            ShowTimeDrawerContent(menuActions = menuActions)
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isFloatingChrome) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .zIndex(1f)
                ) {
                    when (chromeType) {
                        NavigationChromeType.CompactRail -> {
                            ShowTimeNavigationRail(
                                topLevelNavKey = navigationState.topLevelRoute,
                                onTopLevelNavItemSelected = { navItem ->
                                    navigator.navigate(navItem.navKey)
                                },
                                onMenuClick = onOpenDrawer,
                                onOpenTheme = menuActions.onOpenTheme,
                                onOpenPro = menuActions.onOpenPro
                            )
                        }

                        NavigationChromeType.PermanentSidebar -> {
                            ShowTimeDesktopSidebar(
                                currentNavKey = currentDestination,
                                topLevelNavKey = navigationState.topLevelRoute,
                                onTopLevelNavItemSelected = { navItem ->
                                    navigator.navigate(navItem.navKey)
                                },
                                menuActions = menuActions
                            )
                        }

                        NavigationChromeType.FloatingBottomBar -> Unit
                    }
                }
            }

            val workspaceModifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds()
                .then(
                    if (isFloatingChrome) {
                        Modifier.nestedScroll(bottomBarNestedScrollConnection)
                    } else {
                        Modifier
                    }
                )

            SharedTransitionLayout(modifier = workspaceModifier) {
                CompositionLocalProvider(
                    LocalFloatingBarsVisible provides (isFloatingChrome && isBottomBarVisible),
                    LocalSharedTransitionScope provides this
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (!isFloatingChrome) {
                            ShowTimeDesktopTopBar(
                                googleUser = googleUser,
                                isProActive = isProActive,
                                watchProviderRegion = watchProviderRegion,
                                preferredOriginalLanguage = preferredOriginalLanguage,
                                onSearchClick = { navigator.navigate(SearchNavKey) },
                                onProfileClick = { navigator.navigate(ProfileNavKey) },
                                onLocalizationClick = onOpenLocalization,
                                onResetLanguageFilter = onResetLanguageFilter,
                                onOpenKeyboardShortcuts = menuActions.onOpenKeyboardShortcuts,
                                showSearchTrigger = currentDestination !is SearchNavKey
                            )
                        }

                        val contentHostModifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .clipToBounds()
                            .then(
                                if (!isFloatingChrome) {
                                    Modifier.consumeWindowInsets(topBarInsets)
                                } else {
                                    Modifier
                                }
                            )

                        Box(modifier = contentHostModifier) {
                            ShowTimeNavDisplay(
                                navigationState = navigationState,
                                navigator = navigator,
                                openLibraryPage = { navKey -> navigator.navigate(navKey) },
                                modifier = Modifier.fillMaxSize()
                            )

                            MobileFloatingOverlayBars(
                                visible = isFloatingChrome && isHomePage && isBottomBarVisible,
                                navigationState = navigationState,
                                navigator = navigator,
                                currentDestination = currentDestination,
                                googleUser = googleUser,
                                isProActive = isProActive,
                                watchProviderRegion = watchProviderRegion,
                                preferredOriginalLanguage = preferredOriginalLanguage,
                                onOpenLocalization = onOpenLocalization,
                                onResetLanguageFilter = onResetLanguageFilter,
                                onOpenDrawer = onOpenDrawer
                            )

                            ShowTimeSnackbarHost(
                                hostState = snackbarHostState,
                                floatingBottomBar = isFloatingChrome && isHomePage && isBottomBarVisible,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MobileFloatingOverlayBars(
    visible: Boolean,
    navigationState: NavigationState,
    navigator: Navigator,
    currentDestination: NavKey?,
    googleUser: GoogleUser?,
    isProActive: Boolean,
    watchProviderRegion: String,
    preferredOriginalLanguage: String,
    onOpenLocalization: () -> Unit,
    onResetLanguageFilter: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(animationSpec = tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(animationSpec = tween(220)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .windowTopBarInsetsPadding()
    ) {
        CompositionLocalProvider(
            LocalNavAnimatedVisibilityScope provides this
        ) {
            ShowTimeTopSearchBar(
                googleUser = googleUser,
                isProActive = isProActive,
                watchProviderRegion = watchProviderRegion,
                preferredOriginalLanguage = preferredOriginalLanguage,
                onLocalizationClick = onOpenLocalization,
                onResetLanguageFilter = onResetLanguageFilter,
                onMenuClick = onOpenDrawer,
                onSearchClick = { navigator.navigate(SearchNavKey) },
                onProfileClick = { navigator.navigate(ProfileNavKey) }
            )
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(animationSpec = tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(animationSpec = tween(220)),
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
