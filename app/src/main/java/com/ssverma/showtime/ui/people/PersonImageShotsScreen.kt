package com.ssverma.showtime.ui.people

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.common.AppTopAppBar
import com.ssverma.showtime.ui.common.PagedContent
import com.ssverma.showtime.ui.common.PagedGrid
import com.ssverma.showtime.ui.movie.ImageShotItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PersonImageShotsScreen(
    viewModel: PersonImagesViewModel,
    onBackPressed: () -> Unit
) {
    val imageShots = viewModel.personImages.collectAsLazyPagingItems()

    var clickedImageShot: ImageShot? by remember { mutableStateOf(null) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                BackHandler {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }
            }

            Column(Modifier.statusBarsPadding()) {
                AppTopAppBar(
                    title = "",
                    onBackPressed = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                    navIconRes = R.drawable.ic_expand_more,
                    elevation = 0.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                clickedImageShot?.let {
                    ImageShotItem(
                        imageShot = it,
                        onClick = {
                            //Not needed
                        },
                    )
                }
            }
        },
        sheetBackgroundColor = MaterialTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .statusBarsPadding()
        ) {
            AppTopAppBar(
                title = stringResource(id = R.string.tagged_images),
                onBackPressed = onBackPressed
            )
            PagedContent(pagingItems = imageShots) {
                PagedGrid(
                    pagingItems = imageShots,
                    cells = GridCells.Adaptive(minSize = 100.dp),
                ) { imageShot ->
                    ImageShotItem(
                        imageShot = imageShot,
                        onClick = {
                            clickedImageShot = imageShot
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(imageShots[0]?.aspectRatio ?: 0f)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}