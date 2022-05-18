package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.ImageShotItem

@Composable
fun ImageShotsListScreen(
    onBackPressed: () -> Unit,
    openImagePager: (pageIndex: Int) -> Unit,
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.statusBarsPadding()) {
        ShowTimeTopAppBar(
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageShotsContent(
    imageShots: List<ImageShot>,
    openImagePager: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
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