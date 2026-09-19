package com.ssverma.showtime

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.background
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.ssverma.core.ui.util.findActivity
import com.ssverma.core.ui.util.openWebUrl
import androidx.navigation3.runtime.NavKey
import com.ssverma.common.ui.appinfo.AppInfoBottomSheet
import com.ssverma.common.ui.appinfo.OpenSourceLicensesBottomSheet
import com.ssverma.common.ui.paywall.ProPaywallBottomSheet
import com.ssverma.common.ui.state.LocalAppInfoTrigger
import com.ssverma.common.ui.state.LocalAppStateHolder
import com.ssverma.common.ui.theme.ThemeSelectionBottomSheet
import com.ssverma.core.navigation.nav3.LocalNavAnimatedVisibilityScope
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.rememberNavigationState
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.layout.LocalFloatingBarsVisible
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.feature.account.navigation.BackupSyncNavKey
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.utils.AppConfigConstants
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.shared.ui.component.LocalizationSettingsBottomSheet
import com.ssverma.showtime.component.ShowTimeBottomBar
import com.ssverma.showtime.component.ShowTimeDrawerContent
import com.ssverma.showtime.component.ShowTimeHomeBackHandler
import com.ssverma.showtime.component.ShowTimeTopSearchBar
import com.ssverma.showtime.component.isHomePage
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.navigation.DashboardHomeNavKey
import com.ssverma.showtime.navigation.OnboardingNavKey
import com.ssverma.showtime.navigation.ShowTimeNavDisplay
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems
import com.ssverma.showtime.navigation.WhatsNewNavKey
import com.ssverma.showtime.ui.onboarding.OnboardingScreen
import com.ssverma.showtime.ui.splash.ShowTime2Dot0SplashScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ShowTime(
    initialDeepLinkKey: NavKey? = null
) {
    val appStateHolder = LocalAppStateHolder.current
    val appTheme by appStateHolder.appTheme.collectAsState()
    val isDynamicColorEnabled by appStateHolder.isDynamicColorEnabled.collectAsState(initial = false)
    val isProActive by appStateHolder.isProActive.collectAsState(initial = false)

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

        val context = LocalContext.current
        val view = LocalView.current
        if (!view.isInEditMode) {
            DisposableEffect(darkTheme) {
                val activity = context.findActivity() as? ComponentActivity
                val barStyle = SystemBarStyle.auto(
                    lightScrim = android.graphics.Color.TRANSPARENT,
                    darkScrim = android.graphics.Color.TRANSPARENT,
                    detectDarkMode = { _ -> darkTheme }
                )
                activity?.enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
                val window = activity?.window ?: context.findActivity()?.window
                if (window != null) {
                    val insetsController =
                        WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }
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
                    onSplashComplete = {
                        replaySplash = false
                    }
                )
            }

            isFreshInstall -> {
                if (!hasStartedOnboardingJourney) {
                    ShowTime2Dot0SplashScreen(
                        onSplashComplete = {
                            hasStartedOnboardingJourney = true
                        }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
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

    val topLevelRoutes = remember {
        ShowTimeTopLevelNavItems.map { it.navKey }.toSet()
    }

    val navigationState = rememberNavigationState(
        startRoute = DashboardHomeNavKey,
        topLevelRoutes = topLevelRoutes
    )

    val navigator = remember(navigationState) { Navigator(navigationState) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val homeSnackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialDeepLinkKey) {
        initialDeepLinkKey?.let { navigator.navigate(it) }
    }

    val hasCompletedOnboarding by appStateHolder.hasCompletedOnboarding.collectAsState()
    val isAppInfoDismissed by appStateHolder.isAppInfoDismissed.collectAsState()
    var showManualAppInfoSheet by remember { mutableStateOf(false) }
    var showLicensesSheet by remember { mutableStateOf(false) }
    var showThemeSelectionSheet by remember { mutableStateOf(false) }
    var showLocalizationSettingsSheet by remember { mutableStateOf(false) }
    var showProPaywallSheet by remember { mutableStateOf(false) }

    if (showManualAppInfoSheet) {
        AppInfoBottomSheet(
            showDontShowAgain = false,
            onDismissRequest = {
                showManualAppInfoSheet = false
            },
            onOpenLicenses = {
                showManualAppInfoSheet = false
                showLicensesSheet = true
            }
        )
    }

    if (showLicensesSheet) {
        OpenSourceLicensesBottomSheet(
            onDismissRequest = {
                showLicensesSheet = false
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

    LaunchedEffect(
        hasCompletedOnboarding,
        isAppInfoDismissed
    ) {
        val completed = hasCompletedOnboarding ?: return@LaunchedEffect
        if (!completed && isAppInfoDismissed) {
            // Upgraded 1.x user who previously dismissed AppInfo: mark onboarding completed
            appStateHolder.markOnboardingCompleted()
        }
    }

    val currentDestination =
        navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

    CompositionLocalProvider(
        LocalAppInfoTrigger provides { showManualAppInfoSheet = true }
    ) {
        val isHomePage = isHomePage(
            currentNavKey = currentDestination,
            bottomNavDestinations = ShowTimeTopLevelNavItems
        )
        val isAnySheetOpen = showManualAppInfoSheet || showLicensesSheet ||
                showThemeSelectionSheet || showLocalizationSettingsSheet || showProPaywallSheet

        ShowTimeHomeBackHandler(
            drawerState = drawerState,
            navigationState = navigationState,
            navigator = navigator,
            snackbarHostState = homeSnackbarHostState,
            isHomePage = isHomePage,
            isAnySheetOpen = isAnySheetOpen
        )

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = isHomePage,
            drawerContent = {
                ShowTimeDrawerContent(
                    onOpenDiscovery = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(UniversalDiscoveryNavKey())
                    },
                    onOpenMovieMatch = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(MatchRoomNavKey())
                    },
                    onOpenCinemaDiary = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(CinemaDiaryNavKey)
                    },
                    onOpenTasteProfile = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(TasteProfileNavKey)
                    },
                    onOpenWrapped = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(CinephileWrappedNavKey)
                    },
                    onOpenBacklogChallenges = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(BacklogChallengeNavKey)
                    },
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
                        context.openWebUrl(
                            url = AppConfigConstants.PRIVACY_POLICY_URL,
                            onError = { showManualAppInfoSheet = true }
                        )
                    },
                    onOpenTerms = {
                        coroutineScope.launch { drawerState.close() }
                        context.openWebUrl(
                            url = AppConfigConstants.TERMS_OF_SERVICE_URL,
                            onError = { showManualAppInfoSheet = true }
                        )
                    },
                    onOpenLicenses = {
                        coroutineScope.launch { drawerState.close() }
                        showLicensesSheet = true
                    },
                    onOpenAbout = {
                        coroutineScope.launch { drawerState.close() }
                        showManualAppInfoSheet = true
                    },
                    onOpenWhatsNew = {
                        coroutineScope.launch { drawerState.close() }
                        navigator.navigate(WhatsNewNavKey)
                    },
                    onReplaySplash = {
                        coroutineScope.launch { drawerState.close() }
                        onReplaySplash()
                    }
                )
            }
        ) {
            var isBottomBarVisible by rememberSaveable { mutableStateOf(true) }

            LaunchedEffect(currentDestination) {
                isBottomBarVisible = true
            }

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = {
                    ShowTimeSnackbarHost(
                        hostState = homeSnackbarHostState,
                        floatingBottomBar = isHomePage && isBottomBarVisible
                    )
                }
            ) { innerPaddingModifier ->

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
                                        watchProviderRegion = watchProviderRegion,
                                        preferredOriginalLanguage = preferredOriginalLanguage,
                                        onLocalizationClick = {
                                            showLocalizationSettingsSheet = true
                                        },
                                        onResetLanguageFilter = {
                                            appStateHolder.resetPreferredOriginalLanguage()
                                        },
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
