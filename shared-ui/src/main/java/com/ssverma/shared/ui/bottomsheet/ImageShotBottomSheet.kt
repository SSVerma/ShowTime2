package com.ssverma.shared.ui.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.shared.domain.model.ImageShot
import kotlinx.coroutines.launch

sealed interface SheetContentType {
    object ImageList : SheetContentType
    data class ImagePager(val tappedImageIndex: Int) : SheetContentType
    object None : SheetContentType
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageShotBottomSheet(
    imageShots: List<ImageShot>,
    modifier: Modifier = Modifier,
    sheetState: ImageShotBottomSheetState,
    content: @Composable (PaddingValues) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        content(PaddingValues())

        if (sheetState.isVisible) {
            ShowTimeBottomSheet(
                onDismissRequest = { sheetState.hide() },
                sheetState = sheetState.modalBottomSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = null,
                modifier = Modifier.fillMaxSize()
            ) {
                ImageSheetContent(
                    imageShots = imageShots,
                    sheetContentType = sheetState.sheetContentType,
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
            }
        }
    }
}

@Composable
private fun ImageSheetContent(
    imageShots: List<ImageShot>,
    sheetContentType: SheetContentType,
    openImagePager: (pageIndex: Int) -> Unit,
    onBackPress: () -> Unit
) {
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
            ImagePagerContent(
                imageShots = imageShots,
                defaultPageIndex = sheetContentType.tappedImageIndex,
                onBackPressed = {
                    onBackPress()
                }
            )
        }

        SheetContentType.None -> {
            /* no-op */
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberImageShotBottomSheetState(): ImageShotBottomSheetState {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    return remember(modalBottomSheetState) {
        ImageShotBottomSheetState(modalBottomSheetState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class ImageShotBottomSheetState(
    val modalBottomSheetState: SheetState
) {
    var sheetContentType by mutableStateOf<SheetContentType>(SheetContentType.None)
        private set

    var isVisible by mutableStateOf(false)
        private set

    suspend fun show(contentType: SheetContentType) {
        sheetContentType = contentType
        isVisible = true
        modalBottomSheetState.show()
    }

    fun hide() {
        isVisible = false
        sheetContentType = SheetContentType.None
    }

    suspend fun showPreviousOrCollapse() {
        // For now, just collapse. This can be enhanced to handle history.
        modalBottomSheetState.hide()
        hide()
    }
}
