package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.modifier.zoomable
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.ui.component.ImageShotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePagerScreen(
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

    // Reset current scale when the page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            currentItemScale = 1f
        }
    }

    Box(modifier) {
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
    }
}
