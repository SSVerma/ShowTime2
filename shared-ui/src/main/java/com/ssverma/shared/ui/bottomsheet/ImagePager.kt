package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.ui.component.ImageShotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePagerScreen(
    imageShots: List<ImageShot>,
    onBackPressed: () -> Unit,
    defaultPageIndex: Int = 0
) {
    Column(
        modifier = Modifier
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

    val pagerState = rememberPagerState(pageCount = { imageShots.size })

    Box(modifier) {
        HorizontalPager(state = pagerState) { page ->
            ImageShotItem(
                imageShot = imageShots[page],
                onClick = {},
                backgroundColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            )
        }
    }

    LaunchedEffect(pagerState, defaultPageIndex) {
        if (defaultPageIndex >= 0 && defaultPageIndex < imageShots.size) {
            pagerState.scrollToPage(defaultPageIndex)
        }
    }
}
