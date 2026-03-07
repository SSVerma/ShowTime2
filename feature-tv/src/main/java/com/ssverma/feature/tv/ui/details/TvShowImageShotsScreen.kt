package com.ssverma.feature.tv.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.tv.navigation.TvShowDetailDestination
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

@Composable
fun TvShowImageShotsScreen(
    navController: NavController,
    onBackPressed: () -> Unit,
    openImagePager: (Int) -> Unit,
    viewModel: TvShowDetailsViewModel = navController.destinationViewModel(destination = TvShowDetailDestination)
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImageShotsListScreen(
        imageShots = imageShots,
        onBackPressed = onBackPressed,
        openImagePager = openImagePager
    )
}
