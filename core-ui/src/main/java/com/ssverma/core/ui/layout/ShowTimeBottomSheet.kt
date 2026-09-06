package com.ssverma.core.ui.layout

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import androidx.navigationevent.OnBackInvokedDefaultInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch

/**
 * Universal, highly-polished Modal Bottom Sheet for ShowTime.
 *
 * Enforces:
 * 1. Root-level Back navigation handling with Predictive Back animations:
 *    - Real-time gesture progress tracking: smoothly scales, translates, and softens alpha during edge swipes.
 *    - Animated close transitions: smoothly hides down the sheet before calling [onDismissRequest].
 *    - Dialog window back dispatcher bridge on Android 13+ (API 33+).
 * 2. Full-height expansion up to top safe drawing insets ([skipPartiallyExpanded] = true).
 * 3. Smooth nested scrolling where content scrolls up first and dragging down only dismisses
 *    when the scrollable content reaches the top.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimeBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    ),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = {
        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
    },
    sheetGesturesEnabled: Boolean = true,
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(
        shouldDismissOnBackPress = false
    ),
    isBackEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isClosing by remember { mutableStateOf(false) }

    val animateDismiss: () -> Unit = {
        if (!isClosing) {
            isClosing = true
            coroutineScope.launch {
                try {
                    sheetState.hide()
                } catch (_: Throwable) {
                } finally {
                    onDismissRequest()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isSheetBackEnabled = isBackEnabled && !isClosing && isResumed &&
            (sheetState.isVisible || sheetState.targetValue != SheetValue.Hidden)

    val navEventOwner = LocalNavigationEventDispatcherOwner.current
    val sheetGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    if (navEventOwner != null) {
        NavigationBackHandler(
            state = sheetGestureState,
            isBackEnabled = isSheetBackEnabled,
            onBackCompleted = {
                animateDismiss()
            }
        )
    }

    val gestureTransition = sheetGestureState.transitionState
    val inPredictiveBack = gestureTransition is InProgress
    val progress = if (gestureTransition is InProgress) {
        gestureTransition.latestEvent.progress
    } else {
        0f
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        sheetGesturesEnabled = sheetGesturesEnabled,
        properties = properties
    ) {
        val view = LocalView.current
        val dispatcher = navEventOwner?.navigationEventDispatcher

        if (dispatcher != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            DisposableEffect(view, dispatcher) {
                val invoker = view.findOnBackInvokedDispatcher()
                if (invoker != null) {
                    val input = OnBackInvokedDefaultInput(invoker)
                    dispatcher.addInput(input)
                    onDispose {
                        dispatcher.removeInput(input)
                    }
                } else {
                    onDispose {}
                }
            }
        }

        BackHandler(enabled = isSheetBackEnabled) {
            animateDismiss()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    if (inPredictiveBack) {
                        val scale = 1f - (progress * 0.08f)
                        scaleX = scale
                        scaleY = scale
                        translationY = progress * 100.dp.toPx()
                        alpha = 1f - (progress * 0.2f)
                    }
                }
        ) {
            content()
        }
    }
}
