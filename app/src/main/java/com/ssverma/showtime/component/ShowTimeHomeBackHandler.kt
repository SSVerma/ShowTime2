package com.ssverma.showtime.component

import androidx.activity.compose.BackHandler
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ssverma.core.navigation.nav3.NavigationState
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.util.findActivity
import com.ssverma.showtime.R
import kotlinx.coroutines.launch

@Composable
fun ShowTimeHomeBackHandler(
    drawerState: DrawerState,
    navigationState: NavigationState,
    navigator: Navigator,
    snackbarHostState: SnackbarHostState,
    isHomePage: Boolean,
    isAnySheetOpen: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navEventOwner = LocalNavigationEventDispatcherOwner.current

    // Priority 1: If Drawer is open, back closes the drawer first
    val isDrawerOpen = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open
    val drawerGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    if (navEventOwner != null) {
        NavigationBackHandler(
            state = drawerGestureState,
            isBackEnabled = isDrawerOpen,
            onBackCompleted = {
                coroutineScope.launch { drawerState.close() }
            }
        )
    }
    BackHandler(enabled = isDrawerOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    // Priority 2: If on another top-level tab at base, back switches back to the start tab (Home)
    val isOtherTopLevelAtBase = isHomePage && !isDrawerOpen && !isAnySheetOpen &&
            navigationState.topLevelRoute != navigationState.startRoute &&
            (navigationState.backStacks[navigationState.topLevelRoute]?.size ?: 0) <= 1

    val tabGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    if (navEventOwner != null) {
        NavigationBackHandler(
            state = tabGestureState,
            isBackEnabled = isOtherTopLevelAtBase,
            onBackCompleted = {
                navigator.goBack()
            }
        )
    }
    BackHandler(enabled = isOtherTopLevelAtBase) {
        navigator.goBack()
    }

    // Priority 3: If on the Home tab at base, double-back within 2 seconds exits the app
    val isAtHomeRoot = isHomePage && !isDrawerOpen && !isAnySheetOpen &&
            navigationState.topLevelRoute == navigationState.startRoute &&
            (navigationState.backStacks[navigationState.startRoute]?.size ?: 0) <= 1

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitMessage = stringResource(id = R.string.press_back_again_to_exit)

    val homeExitGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    val handleHomeBack: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < BACK_PRESS_EXIT_THRESHOLD_MS) {
            context.findActivity()?.finish()
        } else {
            lastBackPressTime = now
            coroutineScope.launch {
                snackbarHostState.showImmediateSnackbar(
                    message = exitMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (navEventOwner != null) {
        NavigationBackHandler(
            state = homeExitGestureState,
            isBackEnabled = isAtHomeRoot,
            onBackCompleted = handleHomeBack
        )
    }
    BackHandler(enabled = isAtHomeRoot) {
        handleHomeBack()
    }
}

private const val BACK_PRESS_EXIT_THRESHOLD_MS = 2000L
