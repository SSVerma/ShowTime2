package com.ssverma.feature.movie.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.shared.ui.bottomsheet.ImagePagerContent

@Composable
fun MovieImagePagerScreen(
    defaultPageIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: MovieDetailsViewModel
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImagePagerContent(
        imageShots = imageShots,
        defaultPageIndex = defaultPageIndex,
        onBackPressed = onBackPressed
    )
}
