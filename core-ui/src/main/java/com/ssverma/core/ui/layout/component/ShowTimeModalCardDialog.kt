package com.ssverma.core.ui.layout.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ssverma.core.ui.adaptive.AdaptiveGridDefaults
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.theme.spacing

/**
 * Centered floating modal card presentation used by ShowTimeBottomSheet on Medium and Large
 * tablet/desktop viewports so overlays never stretch across 1200dp+ monitors.
 */
@Composable
internal fun ShowTimeModalCardDialog(
    onDismissRequest: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    tonalElevation: Dp,
    isBackEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val adaptiveLayoutInfo = LocalAdaptiveLayoutInfo.current
    val maxCardHeight = (adaptiveLayoutInfo.windowHeightDp * ModalCardMaxHeightRatio)
        .coerceAtLeast(ModalCardMinHeight)

    val navEventOwner = LocalNavigationEventDispatcherOwner.current
    val dialogGestureState = rememberNavigationEventState(
        currentInfo = remember { object : NavigationEventInfo() {} }
    )

    if (navEventOwner != null) {
        NavigationBackHandler(
            state = dialogGestureState,
            isBackEnabled = isBackEnabled,
            onBackCompleted = onDismissRequest
        )
    }

    val gestureTransition = dialogGestureState.transitionState
    val inPredictiveBack = gestureTransition is InProgress
    val backProgress = if (gestureTransition is InProgress) {
        gestureTransition.latestEvent.progress
    } else {
        0f
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isBackEnabled,
            dismissOnClickOutside = true
        )
    ) {
        BackHandler(enabled = isBackEnabled) {
            onDismissRequest()
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(MaterialTheme.spacing.large)
                .onPreviewKeyEvent { keyEvent ->
                    if (isBackEnabled && keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Escape) {
                        onDismissRequest()
                        true
                    } else {
                        false
                    }
                }
        ) {
            Surface(
                shape = RoundedCornerShape(ModalCardCornerRadius),
                color = containerColor,
                contentColor = contentColor,
                tonalElevation = tonalElevation,
                shadowElevation = MaterialTheme.spacing.small,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = modifier
                    .widthIn(max = AdaptiveGridDefaults.ModalCardMaxWidth)
                    .heightIn(max = maxCardHeight)
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (inPredictiveBack) {
                            val scale = 1f - (backProgress * 0.06f)
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - (backProgress * 0.2f)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.medium),
                    content = content
                )
            }
        }
    }
}

private const val ModalCardMaxHeightRatio = 0.86f
private val ModalCardMinHeight = 320.dp
private val ModalCardCornerRadius = 28.dp
