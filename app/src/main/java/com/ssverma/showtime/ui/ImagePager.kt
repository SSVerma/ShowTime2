package com.ssverma.showtime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.movie.ImageShotItem

@Composable
fun ImagePagerScreen(
    imageShots: List<ImageShot>,
    onBackPressed: () -> Unit,
    defaultPageIndex: Int = 0
) {
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .background(color = MaterialTheme.colors.background)
    ) {
        ShowTimeTopAppBar(
            title = "",
            onBackPressed = onBackPressed,
            navIcon = AppIcons.Close,
            elevation = 0.dp,
        )
        ImagePager(
            imageShots = imageShots,
            defaultPageIndex = defaultPageIndex,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ImagePagerScreen(
    observableImageShots: State<List<ImageShot>>,
    onBackPressed: () -> Unit,
    defaultPageIndex: Int = 0
) {
    val imageShots by observableImageShots

    ImagePagerScreen(
        imageShots = imageShots,
        onBackPressed = onBackPressed,
        defaultPageIndex = defaultPageIndex
    )
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagePager(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    defaultPageIndex: Int = 0
) {
    if (imageShots.isEmpty()) {
        return
    }

    val pagerState = rememberPagerState()

    Box(modifier) {
        HorizontalPager(state = pagerState, count = imageShots.size) { page ->
            ImageShotItem(
                imageShot = imageShots[page],
                onClick = {},
                backgroundColor = MaterialTheme.colors.background,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            )
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            indicatorWidth = 4.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(vertical = 16.dp, horizontal = 32.dp)
        )
    }

    LaunchedEffect(pagerState, defaultPageIndex) {
        if (defaultPageIndex >= 0 && defaultPageIndex < imageShots.size) {
            pagerState.scrollToPage(defaultPageIndex)
        }
    }
}