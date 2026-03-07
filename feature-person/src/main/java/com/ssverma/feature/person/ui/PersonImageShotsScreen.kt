package com.ssverma.feature.person.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.person.R
import com.ssverma.shared.ui.component.ImageShotItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        modifier = Modifier.statusBarsPadding(),
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                BackHandler {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    }
                }
            }

            Column(Modifier.statusBarsPadding()) {
                ShowTimeTopAppBar(
                    title = "",
                    onBackPressed = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    navIcon = Icons.Default.KeyboardArrowDown
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
        sheetContainerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
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
                            .aspectRatio(imageShots[0]?.aspectRatio ?: 1f)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
