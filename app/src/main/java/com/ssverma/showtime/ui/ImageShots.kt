package com.ssverma.showtime.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.common.AppTopAppBar
import com.ssverma.showtime.ui.movie.ImageShotItem

@Composable
fun ImageShotsListScreen(
    onBackPressed: () -> Unit,
    openImagePager: (pageIndex: Int) -> Unit,
    imageShots: List<ImageShot>
) {
    Column(modifier = Modifier.statusBarsPadding()) {
        AppTopAppBar(
            title = stringResource(id = R.string.shots),
            onBackPressed = onBackPressed
        )
        ImageShotsContent(
            imageShots = imageShots,
            openImagePager = openImagePager,
            modifier = Modifier
                .navigationBarsPadding()
        )
    }
}

@Composable
fun ImageShotsListScreen(
    onBackPressed: () -> Unit,
    openImagePager: (pageIndex: Int) -> Unit,
    liveImageShots: LiveData<List<ImageShot>>
) {
    val imageShots by liveImageShots.observeAsState(initial = emptyList())

    ImageShotsListScreen(
        onBackPressed = onBackPressed,
        openImagePager = openImagePager,
        imageShots = imageShots
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageShotsContent(
    imageShots: List<ImageShot>,
    openImagePager: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyVerticalGrid(
        cells = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        itemsIndexed(imageShots) { index, item ->
            ImageShotItem(
                imageShot = item,
                onClick = { openImagePager(index) },
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(imageShots.first().aspectRatio)
                    .padding(4.dp)
            )
        }
    }
}