package com.ssverma.feature.tv.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

@Composable
fun TvShowImageShotsScreen(
    onBackPressed: () -> Unit,
    openImagePager: (Int) -> Unit,
    viewModel: TvShowDetailsViewModel
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImageShotsListScreen(
        imageShots = imageShots,
        onBackPressed = onBackPressed,
        openImagePager = openImagePager
    )
}
