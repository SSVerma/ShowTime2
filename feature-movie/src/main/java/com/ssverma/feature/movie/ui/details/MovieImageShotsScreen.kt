package com.ssverma.feature.movie.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

@Composable
fun MovieImageShotsScreen(
    onBackPressed: () -> Unit,
    openImagePager: (Int) -> Unit,
    viewModel: MovieDetailsViewModel
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImageShotsListScreen(
        imageShots = imageShots,
        onBackPressed = onBackPressed,
        openImagePager = openImagePager
    )
}
