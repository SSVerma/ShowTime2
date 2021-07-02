package com.ssverma.showtime.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.common.AppTopAppBar
import com.ssverma.showtime.ui.common.StaggeredVerticalGrid
import com.ssverma.showtime.ui.movie.ImageShotItem

@Composable
fun ImageShotsListScreen(
    onBackPressed: () -> Unit,
    openImagePager: (pageIndex: Int) -> Unit,
    liveImageShots: LiveData<List<ImageShot>>
) {
    val imageShots by liveImageShots.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier.statusBarsPadding()
    ) {
        AppTopAppBar(
            title = stringResource(id = R.string.shots),
            onBackPressed = onBackPressed
        )
        ImageShotsContent(
            imageShots = imageShots,
            openImagePager = openImagePager,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotsContent(
    imageShots: List<ImageShot>,
    openImagePager: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    columnWidth: Dp = 160.dp
) {
    StaggeredVerticalGrid(
        items = imageShots,
        maxColumnWidth = columnWidth,
        modifier = modifier
    ) { index, item ->
        ImageShotItem(
            imageShot = item,
            onClick = {
                openImagePager(index)
            },
            modifier = Modifier.padding(2.dp)
        )
    }
}