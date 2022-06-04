package com.ssverma.feature.person.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.person.R
import com.ssverma.shared.ui.component.ImageShotItem
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
                ShowTimeTopAppBar(
                    title = "",
                    onBackPressed = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                    navIcon = AppIcons.KeyboardArrowDown,
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
            ShowTimeTopAppBar(
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