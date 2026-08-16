package com.ssverma.feature.person.ui.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.feature.person.ui.details.content.PersonDetailsContent
import com.ssverma.shared.ui.bottomsheet.ImagePagerContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailsScreen(
    personId: Int,
    initialName: String? = null,
    initialImageUrl: String? = null,
    onBackPress: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openPersonAllImages: (personId: Int) -> Unit,
    viewModel: PersonDetailsViewModel
) {
    TrackScreenView(screenName = PersonAnalyticsScreenName.PERSON_DETAILS)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var profileImagePageIndex by remember { mutableIntStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        PersonDetailsContent(
            personId = personId,
            initialName = initialName,
            initialImageUrl = initialImageUrl,
            personState = viewModel.personDetailUiState,
            onRetry = { viewModel.fetchPersonDetails() },
            onBackPress = onBackPress,
            openImagePage = { pageIndex ->
                profileImagePageIndex = pageIndex
                showSheet = true
            },
            openMovieDetails = openMovieDetails,
            openTvShowDetails = openTvShowDetails,
            openPersonAllImages = openPersonAllImages,
            modifier = Modifier.fillMaxSize()
        )

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = null,
                modifier = Modifier.fillMaxSize()
            ) {
                ImagePagerContent(
                    imageShots = viewModel.imageShots,
                    defaultPageIndex = profileImagePageIndex,
                    onBackPressed = {
                        coroutineScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}
