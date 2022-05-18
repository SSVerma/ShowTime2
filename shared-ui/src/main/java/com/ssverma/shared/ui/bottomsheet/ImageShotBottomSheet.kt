package com.ssverma.shared.ui.bottomsheet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ssverma.core.domain.model.ImageShot
import kotlinx.coroutines.launch

sealed interface SheetContentType {
    object ImageList : SheetContentType
    data class ImagePager(val tappedImageIndex: Int) : SheetContentType
    object None : SheetContentType
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotBottomSheet(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    sheetState: ImageShotBottomSheetState,
    content: @Composable (PaddingValues) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = sheetState.bottomSheetScaffoldState,
        sheetContent = {
            ImageSheetContent(
                imageShots = imageShots,
                sheetContentType = sheetState.sheetContentType,
                sheetVisible = sheetState.expended(),
                openImagePager = { pageIndex ->
                    coroutineScope.launch {
                        sheetState.show(SheetContentType.ImagePager(pageIndex))
                    }
                },
                onBackPress = {
                    coroutineScope.launch {
                        sheetState.showPreviousOrCollapse()
                    }
                }
            )
        },
        sheetPeekHeight = 0.dp,
        sheetBackgroundColor = MaterialTheme.colors.background,
        modifier = modifier.fillMaxSize(),
        content = content
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ImageSheetContent(
    imageShots: List<ImageShot>,
    sheetContentType: SheetContentType,
    sheetVisible: Boolean,
    openImagePager: (pageIndex: Int) -> Unit,
    onBackPress: () -> Unit
) {

    if (sheetVisible) {
        BackHandler {
            onBackPress()
        }
    }
    when (sheetContentType) {
        SheetContentType.ImageList -> {
            ImageShotsListScreen(
                imageShots = imageShots,
                openImagePager = { pageIndex ->
                    openImagePager(pageIndex)
                },
                onBackPressed = {
                    onBackPress()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        is SheetContentType.ImagePager -> {
            ImagePagerScreen(
                imageShots = imageShots,
                defaultPageIndex = sheetContentType.tappedImageIndex,
                onBackPressed = {
                    onBackPress()
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
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberImageShotBottomSheetState(
    bottomSheetScaffoldState: BottomSheetScaffoldState
): ImageShotBottomSheetState {
    return remember {
        ImageShotBottomSheetState(
            bottomSheetScaffoldState = bottomSheetScaffoldState
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
class ImageShotBottomSheetState(
    val bottomSheetScaffoldState: BottomSheetScaffoldState
) {
    var sheetContentType by mutableStateOf<SheetContentType>(SheetContentType.None)
        private set

    suspend fun show(contentType: SheetContentType) {
        sheetContentType = contentType

        if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
            bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }

    suspend fun collapse() {
        bottomSheetScaffoldState.bottomSheetState.collapse()
    }

    suspend fun showPreviousOrCollapse() {
        // TODO: show last destination, update with navigation graph
        collapse()
    }

    fun expended(): Boolean {
        return bottomSheetScaffoldState.bottomSheetState.isExpanded
    }

}