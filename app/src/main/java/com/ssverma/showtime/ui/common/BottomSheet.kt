package com.ssverma.showtime.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.ui.ImagePagerScreen
import com.ssverma.showtime.ui.ImageShotsListScreen
import kotlinx.coroutines.launch

enum class SheetContentType {
    ImageList,
    ImagePager,
    None
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotBottomSheet(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    sheetItem: MutableState<SheetContentType>,
    tappedImageIndex: MutableState<Int>,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var sheetCurrentContentType by remember { sheetItem }
    var clickedImageIndex by remember { tappedImageIndex }

    LaunchedEffect(key1 = sheetCurrentContentType) {
        when (sheetCurrentContentType) {
            SheetContentType.ImageList,
            SheetContentType.ImagePager -> {
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
            SheetContentType.None -> {
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
                        sheetCurrentContentType = SheetContentType.None
                    }
                }
            }
            when (sheetCurrentContentType) {
                SheetContentType.ImageList -> {
                    ImageShotsListScreen(
                        imageShots = imageShots,
                        openImagePager = { pageIndex ->
                            clickedImageIndex = pageIndex
                            sheetCurrentContentType = SheetContentType.ImagePager

                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                        onBackPressed = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                                clickedImageIndex = 0
                                sheetCurrentContentType = SheetContentType.None
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                SheetContentType.ImagePager -> {
                    ImagePagerScreen(
                        imageShots = imageShots,
                        defaultPageIndex = clickedImageIndex,
                        onBackPressed = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                                clickedImageIndex = 0
                                sheetCurrentContentType = SheetContentType.None
                            }
                        }
                    )
                }
                SheetContentType.None -> {
                    //Workaround to prevent no associated anchor exception
                    Box(
                        modifier = Modifier
                            .height(1.dp)
                            .background(color = Color.Transparent)
                    )
                }
            }
        },
        sheetPeekHeight = 0.dp,
        sheetBackgroundColor = MaterialTheme.colors.background,
        modifier = modifier.fillMaxSize(),
        content = content
    )
}