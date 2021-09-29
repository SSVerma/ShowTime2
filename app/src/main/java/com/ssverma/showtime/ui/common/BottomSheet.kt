package com.ssverma.showtime.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.ImageShotsListScreen
import kotlinx.coroutines.launch

enum class SheetItems {
    ImageList,
    ImagePager,
    None
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotBottomSheet(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    sheetItem: MutableState<SheetItems>,
    tappedImageIndex: MutableState<Int>,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var bottomSheetCurrentItem by remember { sheetItem }
    var clickedImageIndex by remember { tappedImageIndex }

    LaunchedEffect(key1 = bottomSheetCurrentItem) {
        when (bottomSheetCurrentItem) {
            SheetItems.ImageList,
            SheetItems.ImagePager -> {
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
            SheetItems.None -> {
                //Do nothing
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                BackHandler {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                        clickedImageIndex = 0
                        bottomSheetCurrentItem = SheetItems.None
                    }
                }
            }
            when (bottomSheetCurrentItem) {
                SheetItems.ImageList -> {
                    ImageShotsListScreen(
                        imageShots = imageShots,
                        openImagePager = { pageIndex ->
                            clickedImageIndex = pageIndex
                            bottomSheetCurrentItem = SheetItems.ImagePager

                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                        onBackPressed = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                                clickedImageIndex = 0
                                bottomSheetCurrentItem = SheetItems.None
                            }
                        }
                    )
                }
                SheetItems.ImagePager -> {
                    ImagePagerScreen(
                        imageShots = imageShots,
                        defaultPageIndex = clickedImageIndex,
                        onBackPressed = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                                clickedImageIndex = 0
                                bottomSheetCurrentItem = SheetItems.None
                            }
                        }
                    )
                }
                SheetItems.None -> {
                    //Do nothing
                }
            }
        },
        sheetPeekHeight = if (bottomSheetCurrentItem == SheetItems.None) 1.dp else 0.dp,
        sheetBackgroundColor = MaterialTheme.colors.background,
        modifier = modifier.fillMaxSize(),
        content = content
    )
}