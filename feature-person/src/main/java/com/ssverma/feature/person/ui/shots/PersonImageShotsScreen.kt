package com.ssverma.feature.person.ui.shots

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.feature.person.R
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.ui.TmdbPersonAspectRatio
import com.ssverma.shared.ui.component.ImageShotItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonImageShotsScreen(
    viewModel: PersonImagesViewModel,
    onBackPressed: () -> Unit
) {
    TrackScreenView(screenName = PersonAnalyticsScreenName.PERSON_IMAGES)

    val imageShots = viewModel.personImages.collectAsLazyPagingItems()

    var clickedImageShot: ImageShot? by remember { mutableStateOf(null) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
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
                            contentScale = ContentScale.Fit,
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
                                .aspectRatio(TmdbPersonAspectRatio)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
