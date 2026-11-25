package com.ssverma.core.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.R
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.ShowTimeWidthClass
import com.ssverma.core.ui.modifier.horizontalWheelScroll
import kotlinx.coroutines.launch

@Composable
inline fun <T> HorizontalLazyList(
    items: List<T>,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    crossinline itemContent: @Composable (item: T) -> Unit
) {
    HorizontalLazyListIndexed(
        items = items,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
    ) { _, item ->
        itemContent(item)
    }
}

@Composable
inline fun <T> HorizontalLazyListIndexed(
    items: List<T>,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    crossinline itemContent: @Composable (index: Int, item: T) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val hoverInteractionSource = remember { MutableInteractionSource() }
    val isHovered by hoverInteractionSource.collectIsHoveredAsState()
    val isDesktopOrTablet = LocalAdaptiveLayoutInfo.current.widthClass != ShowTimeWidthClass.Compact

    Box(
        modifier = Modifier
            .hoverable(interactionSource = hoverInteractionSource)
            .horizontalWheelScroll(
                scrollableState = lazyListState,
                coroutineScope = coroutineScope
            )
    ) {
        LazyRow(
            state = lazyListState,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            content = {
                itemsIndexed(
                    items = items,
                    contentType = { _, _ -> HorizontalListItemContentType }
                ) { index, item ->
                    itemContent(index, item)
                }
            }
        )

        if (isDesktopOrTablet) {
            HorizontalScrollControls(
                lazyListState = lazyListState,
                isHovered = isHovered,
                onScrollBackward = {
                    coroutineScope.launch {
                        lazyListState.animateScrollBy(-DesktopScrollStepPixels)
                    }
                },
                onScrollForward = {
                    coroutineScope.launch {
                        lazyListState.animateScrollBy(DesktopScrollStepPixels)
                    }
                }
            )
        }
    }
}

@PublishedApi
internal const val HorizontalListItemContentType = "horizontal_list_item"

@PublishedApi
internal const val DesktopScrollStepPixels = 420f

@PublishedApi
internal val DesktopScrollPaddleSize = 36.dp

@PublishedApi
internal val DesktopScrollIconSize = 20.dp

@PublishedApi
internal val DesktopScrollPaddleMargin = 8.dp

@PublishedApi
internal val DesktopScrollPaddleElevation = 4.dp

@PublishedApi
@Composable
internal fun BoxScope.HorizontalScrollControls(
    lazyListState: LazyListState,
    isHovered: Boolean,
    onScrollBackward: () -> Unit,
    onScrollForward: () -> Unit
) {
    val canScrollBackward by remember(lazyListState) {
        derivedStateOf { lazyListState.canScrollBackward }
    }
    val canScrollForward by remember(lazyListState) {
        derivedStateOf { lazyListState.canScrollForward }
    }

    AnimatedVisibility(
        visible = isHovered && canScrollBackward,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = DesktopScrollPaddleMargin)
    ) {
        HorizontalScrollPaddleButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(id = R.string.cd_scroll_backward),
            onClick = onScrollBackward
        )
    }

    AnimatedVisibility(
        visible = isHovered && canScrollForward,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = DesktopScrollPaddleMargin)
    ) {
        HorizontalScrollPaddleButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(id = R.string.cd_scroll_forward),
            onClick = onScrollForward
        )
    }
}

@Composable
private fun HorizontalScrollPaddleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = DesktopScrollPaddleElevation,
        shadowElevation = DesktopScrollPaddleElevation,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .size(DesktopScrollPaddleSize)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(DesktopScrollIconSize)
            )
        }
    }
}

@PublishedApi
internal val SectionListContentPadding = PaddingValues(start = 16.dp, end = 16.dp)

@PublishedApi
internal val SectionListHorizontalArrangement = Arrangement.spacedBy(16.dp)
