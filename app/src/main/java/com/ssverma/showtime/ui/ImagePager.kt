package com.ssverma.showtime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.common.AppTopAppBar
import com.ssverma.showtime.ui.movie.ImageShotItem

@Composable
fun ImagePagerScreen(
    liveImageShots: LiveData<List<ImageShot>>,
    onBackPressed: () -> Unit,
    defaultPageIndex: Int = 0
) {
    val imageShots by liveImageShots.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .background(color = MaterialTheme.colors.background)
    ) {
        AppTopAppBar(
            title = "",
            onBackPressed = onBackPressed,
            navIconRes = R.drawable.ic_close,
            elevation = 0.dp,
        )
        ImagePager(
            imageShots = imageShots,
            defaultPageIndex = defaultPageIndex,
            modifier = Modifier.fillMaxSize()
        )
    }
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

    val pagerState = rememberPagerState(pageCount = imageShots.size)

    Box(modifier) {
        HorizontalPager(state = pagerState) { page ->
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