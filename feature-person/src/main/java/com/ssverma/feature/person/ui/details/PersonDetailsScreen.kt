package com.ssverma.feature.person.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.DriveCompose
import com.ssverma.feature.person.ui.details.content.PersonDetailsContent
import com.ssverma.shared.ui.bottomsheet.ImagePagerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailsScreen(
    onBackPress: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openPersonAllImages: (personId: Int) -> Unit,
    viewModel: PersonDetailsViewModel = hiltViewModel(),
) {

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var profileImagePageIndex by remember { mutableIntStateOf(0) }

    val isSheetVisible = bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded

    BackHandler(enabled = isSheetVisible) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.partialExpand()
        }
    }

    DriveCompose(
        uiState = viewModel.personDetailUiState,
        onRetry = { viewModel.fetchPersonDetails() }
    ) { person ->
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetSwipeEnabled = false,
            sheetPeekHeight = 0.dp,
            sheetContent = {
                ImagePagerContent(
                    imageShots = viewModel.imageShots,
                    defaultPageIndex = profileImagePageIndex,
                    onBackPressed = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                )
            },
            sheetContainerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            PersonDetailsContent(
                person = person,
                onBackPress = onBackPress,
                openImagePage = { pageIndex ->
                    profileImagePageIndex = pageIndex
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.expand()
                    }
                },
                openMovieDetails = openMovieDetails,
                openTvShowDetails = openTvShowDetails,
                openPersonAllImages = openPersonAllImages,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
