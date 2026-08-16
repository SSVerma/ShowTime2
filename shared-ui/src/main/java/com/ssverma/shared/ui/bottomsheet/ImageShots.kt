package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.TmdbPersonAspectRatio
import com.ssverma.shared.ui.component.ImageShotItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageShotsListScreen(
    onBackPressed: () -> Unit,
    openImagePager: (pageIndex: Int) -> Unit,
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier
) {
    AppPage(
        modifier = modifier,
        topBar = { scrollBehavior ->
            ShowTimeTopAppBar(
                title = stringResource(id = R.string.shots),
                onBackPressed = onBackPressed,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        ImageShotsContent(
            imageShots = imageShots,
            openImagePager = openImagePager,
            modifier = Modifier
                .padding(innerPadding)
                .navigationBarsPadding()
        )
    }
}

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
                    .aspectRatio(TmdbPersonAspectRatio)
                    .padding(4.dp)
            )
        }
    }
}
