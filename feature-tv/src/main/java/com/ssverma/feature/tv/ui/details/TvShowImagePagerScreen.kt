package com.ssverma.feature.tv.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.shared.ui.bottomsheet.ImagePagerContent

@Composable
fun TvShowImagePagerScreen(
    navController: NavController,
    defaultPageIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: TvShowDetailsViewModel = navController.destinationViewModel(destination = TvShowDetailDestination)
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImagePagerContent(
        imageShots = imageShots,
        defaultPageIndex = defaultPageIndex,
        onBackPressed = onBackPressed
    )
}
