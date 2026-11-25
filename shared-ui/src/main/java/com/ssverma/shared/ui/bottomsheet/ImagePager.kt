package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.ShowTimeWidthClass
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.modifier.zoomable
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.ui.component.ImageShotItem
import kotlinx.coroutines.launch
import com.ssverma.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePagerContent(
    imageShots: List<ImageShot>,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    defaultPageIndex: Int = 0
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        ShowTimeTopAppBar(
            title = "",
            onBackPressed = onBackPressed,
            navIcon = Icons.Default.Close
        )
        ImagePager(
            imageShots = imageShots,
            defaultPageIndex = defaultPageIndex,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ImagePager(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    defaultPageIndex: Int = 0
) {
    if (imageShots.isEmpty()) {
        return
    }

    val initialPage = defaultPageIndex.coerceIn(0, imageShots.size - 1)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { imageShots.size }
    )

    var currentItemScale by remember { mutableFloatStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val isDesktopOrTablet = LocalAdaptiveLayoutInfo.current.widthClass != ShowTimeWidthClass.Compact

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    // Reset current scale when the page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            currentItemScale = 1f
        }
    }

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (keyEvent.key) {
                    Key.DirectionLeft -> {
                        val previousPage = (pagerState.currentPage - 1).coerceAtLeast(0)
                        if (previousPage != pagerState.currentPage) {
                            coroutineScope.launch { pagerState.animateScrollToPage(previousPage) }
                        }
                        true
                    }

                    Key.DirectionRight -> {
                        val nextPage =
                            (pagerState.currentPage + 1).coerceAtMost(imageShots.lastIndex)
                        if (nextPage != pagerState.currentPage) {
                            coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                        }
                        true
                    }

                    else -> false
                }
            }
    ) {
        HorizontalPager(
            state = pagerState,
            key = { page -> imageShots[page].imageUrl },
            // Only disable paging when the current page is significantly zoomed in
            userScrollEnabled = currentItemScale <= 1.05f,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            ImageShotItem(
                imageShot = imageShots[page],
                backgroundColor = MaterialTheme.colorScheme.background,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 16.dp)
                    .zoomable(
                        onScaleChanged = { scale ->
                            // Only update global scale if this is the active page
                            if (pagerState.currentPage == page) {
                                currentItemScale = scale
                            }
                        }
                    ),
            )
        }

        if (isDesktopOrTablet) {
            if (pagerState.currentPage > 0) {
                FilledTonalIconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage - 1).coerceAtLeast(
                                    0
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = MaterialTheme.spacing.medium)
                        .size(PagerNavButtonSize)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = stringResource(id = CoreUiR.string.cd_scroll_backward)
                    )
                }
            }

            if (pagerState.currentPage < imageShots.lastIndex) {
                FilledTonalIconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                (pagerState.currentPage + 1).coerceAtMost(imageShots.lastIndex)
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = MaterialTheme.spacing.medium)
                        .size(PagerNavButtonSize)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(id = CoreUiR.string.cd_scroll_forward)
                    )
                }
            }
        }
    }
}

private val PagerNavButtonSize = 44.dp
