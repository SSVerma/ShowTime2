package com.ssverma.showtime

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.ssverma.common.ui.state.LocalAppInfoTrigger
import com.ssverma.common.ui.state.LocalAppStateHolder
import com.ssverma.common.ui.update.ForceUpdateScreen
import com.ssverma.common.ui.update.SoftUpdateBottomSheet
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.rememberNavigationState
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.NavigationChromeType
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.AppUpdateStatus
import com.ssverma.showtime.component.ShowTimeAdaptiveShell
import com.ssverma.showtime.component.ShowTimeGlobalSheetsHost
import com.ssverma.showtime.component.ShowTimeHomeBackHandler
import com.ssverma.showtime.component.ShowTimeShortcutActions
import com.ssverma.showtime.component.isHomePage
import com.ssverma.showtime.component.rememberShowTimeGlobalSheetState
import com.ssverma.showtime.component.rememberShowTimeMenuActions
import com.ssverma.showtime.component.showTimeGlobalShortcuts
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.navigation.DashboardHomeNavKey
import com.ssverma.showtime.navigation.ShowTimeRailNavItems
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems
import com.ssverma.showtime.ui.onboarding.OnboardingScreen
import com.ssverma.showtime.ui.splash.ShowTime2Dot0SplashScreen
import com.ssverma.showtime.ui.update.AppUpdateViewModel
import com.ssverma.showtime.util.applyShowTimeSystemBars
import kotlinx.coroutines.launch

@Composable
fun ShowTime(
    initialDeepLinkKey: NavKey? = null
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState()
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)
    val isProActive by appStateHolder.isProActive.collectAsState(initial = false)

    val appUpdateViewModel: AppUpdateViewModel = hiltViewModel()
    val updateUiState by appUpdateViewModel.uiState.collectAsStateWithLifecycle()

    ShowTimeTheme(
        appTheme = appTheme,
        dynamicColor = isDynamicColorEnabled
    ) {
        if (updateUiState.status is AppUpdateStatus.ForceUpdate) {
            val forceUpdate = updateUiState.status as AppUpdateStatus.ForceUpdate
            ForceUpdateScreen(
                title = forceUpdate.title,
                message = forceUpdate.message
            )
            return@ShowTimeTheme
        }

        val darkTheme = when (appTheme) {
            AppTheme.System -> isSystemInDarkTheme()
            AppTheme.Light -> false
            AppTheme.Dark,
            AppTheme.OledMidnight -> true
        }

        val context = LocalContext.current
        val density = LocalDensity.current
        val view = LocalView.current
        val hasCaptionBarInset = WindowInsets.captionBar.getTop(density) > 0
        if (!view.isInEditMode) {
            DisposableEffect(darkTheme, hasCaptionBarInset) {
                val activity = context.findActivity() as? ComponentActivity
                if (activity != null) {
                    applyShowTimeSystemBars(
                        activity = activity,
                        isDarkTheme = darkTheme,
                        hasCaptionBarInset = hasCaptionBarInset
                    )
                }
                onDispose {}
            }
        }

        val hasCompletedOnboarding by appStateHolder.hasCompletedOnboarding.collectAsState()
        val isAppInfoDismissed by appStateHolder.isAppInfoDismissed.collectAsState()
        var hasStartedOnboardingJourney by rememberSaveable { mutableStateOf(false) }
        var replaySplash by remember { mutableStateOf(false) }

        val isFreshInstall =
            hasCompletedOnboarding == false && !isAppInfoDismissed && initialDeepLinkKey == null

        if (updateUiState.showSoftUpdatePrompt && updateUiState.status is AppUpdateStatus.SoftUpdate) {
            val softUpdate = updateUiState.status as AppUpdateStatus.SoftUpdate
            SoftUpdateBottomSheet(
                title = softUpdate.title,
                message = softUpdate.message,
                onDismissRequest = {
                    appUpdateViewModel.dismissSoftUpdate(versionCode = softUpdate.latestVersionCode)
                }
            )
        }

        when {
            hasCompletedOnboarding == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            replaySplash -> {
                ShowTime2Dot0SplashScreen(
                    onSplashComplete = { replaySplash = false }
                )
            }

            isFreshInstall -> {
                if (!hasStartedOnboardingJourney) {
                    ShowTime2Dot0SplashScreen(
                        onSplashComplete = { hasStartedOnboardingJourney = true }
                    )
                } else {
                    OnboardingScreen(
                        onCompleteOnboarding = { streamingProviders, genres ->
                            appStateHolder.onCompleteOnboarding(
                                streamingSubscriptions = streamingProviders,
                                seededGenres = genres
                            )
                        }
                    )
                }
            }

            else -> {
                MainDashboardContent(
                    appTheme = appTheme,
                    isDynamicColorEnabled = isDynamicColorEnabled,
                    isProActive = isProActive,
                    initialDeepLinkKey = initialDeepLinkKey,
                    onReplaySplash = { replaySplash = true }
                )
            }
        }
    }
}

@Composable
private fun MainDashboardContent(
    appTheme: AppTheme,
    isDynamicColorEnabled: Boolean,
    isProActive: Boolean,
    initialDeepLinkKey: NavKey? = null,
    onReplaySplash: () -> Unit = {}
) {
    val appStateHolder = LocalAppStateHolder.current
    val googleUser by appStateHolder.googleUser.collectAsState(initial = null)
    val watchProviderRegion by appStateHolder.watchProviderRegion.collectAsState()
    val preferredOriginalLanguage by appStateHolder.preferredOriginalLanguage.collectAsState()
    val context = LocalContext.current

    // Register all 5 rail routes so resizing from Large/Medium down to Compact preserves Person tab stack
    val topLevelRoutes = remember {
        ShowTimeRailNavItems.map { it.navKey }.toSet()
    }

    val navigationState = rememberNavigationState(
        startRoute = DashboardHomeNavKey,
        topLevelRoutes = topLevelRoutes
    )

    val navigator = remember(navigationState) { Navigator(navigationState) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val homeSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val globalSheetState = rememberShowTimeGlobalSheetState()

    LaunchedEffect(initialDeepLinkKey) {
        initialDeepLinkKey?.let { navigator.navigate(it) }
    }

    val hasCompletedOnboarding by appStateHolder.hasCompletedOnboarding.collectAsState()
    val isAppInfoDismissed by appStateHolder.isAppInfoDismissed.collectAsState()

    LaunchedEffect(hasCompletedOnboarding, isAppInfoDismissed) {
        val completed = hasCompletedOnboarding ?: return@LaunchedEffect
        if (!completed && isAppInfoDismissed) {
            appStateHolder.markOnboardingCompleted()
        }
    }

    ShowTimeGlobalSheetsHost(
        sheetState = globalSheetState,
        appStateHolder = appStateHolder,
        appTheme = appTheme,
        isDynamicColorEnabled = isDynamicColorEnabled,
        isProActive = isProActive
    )

    val currentDestination =
        navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

    val chromeType = LocalAdaptiveLayoutInfo.current.navigationChromeType
    val topLevelNavItemsForViewport = if (chromeType == NavigationChromeType.FloatingBottomBar) {
        ShowTimeTopLevelNavItems
    } else {
        ShowTimeRailNavItems
    }

    val closeDrawerBeforeNavigate = remember(drawerState, coroutineScope) {
        {
            if (drawerState.isOpen) {
                coroutineScope.launch { drawerState.close() }
            }
            Unit
        }
    }

    val menuActions = rememberShowTimeMenuActions(
        navigator = navigator,
        context = context,
        sheetState = globalSheetState,
        onReplaySplash = onReplaySplash,
        onBeforeNavigate = closeDrawerBeforeNavigate
    )

    CompositionLocalProvider(
        LocalAppInfoTrigger provides { globalSheetState.showManualAppInfoSheet = true }
    ) {
        val isHomePage = isHomePage(
            currentNavKey = currentDestination,
            bottomNavDestinations = topLevelNavItemsForViewport
        )

        ShowTimeHomeBackHandler(
            drawerState = drawerState,
            navigationState = navigationState,
            navigator = navigator,
            snackbarHostState = homeSnackbarHostState,
            isHomePage = isHomePage,
            isAnySheetOpen = globalSheetState.isAnyOverlayOpen
        )

        val shortcutActions =
            remember(navigator, globalSheetState, drawerState, isHomePage, coroutineScope) {
                ShowTimeShortcutActions(
                    onOpenSearch = { navigator.navigate(SearchNavKey) },
                    onSelectTopLevelTab = { navItem -> navigator.navigate(navItem.navKey) },
                    onOpenCinemaDiary = { navigator.navigate(CinemaDiaryNavKey) },
                    onOpenDiscovery = { navigator.navigate(UniversalDiscoveryNavKey()) },
                    onOpenShortcutsHelp = { globalSheetState.showKeyboardShortcutsDialog = true },
                    onEscape = {
                        when {
                            globalSheetState.dismissAll() -> true
                            drawerState.isOpen -> {
                                coroutineScope.launch { drawerState.close() }
                                true
                            }

                            !isHomePage -> {
                                navigator.goBack()
                                true
                            }

                            else -> false
                        }
                    }
                )
            }

        ShowTimeAdaptiveShell(
            drawerState = drawerState,
            navigationState = navigationState,
            navigator = navigator,
            currentDestination = currentDestination,
            isHomePage = isHomePage,
            googleUser = googleUser,
            isProActive = isProActive,
            watchProviderRegion = watchProviderRegion,
            preferredOriginalLanguage = preferredOriginalLanguage,
            snackbarHostState = homeSnackbarHostState,
            menuActions = menuActions,
            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
            onOpenLocalization = { globalSheetState.showLocalizationSettingsSheet = true },
            onResetLanguageFilter = { appStateHolder.resetPreferredOriginalLanguage() },
            modifier = Modifier.showTimeGlobalShortcuts(
                actions = shortcutActions,
                currentDestinationKey = currentDestination
            )
        )
    }
}
