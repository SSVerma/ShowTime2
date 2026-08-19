package com.ssverma.feature.tv.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.shared.ui.bottomsheet.ImagePagerContent

@Composable
fun TvShowImagePagerScreen(
    defaultPageIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: TvShowDetailsViewModel
) {
    val imageShots by viewModel.imageShots.collectAsStateWithLifecycle()

    ImagePagerContent(
        imageShots = imageShots,
        defaultPageIndex = defaultPageIndex,
        onBackPressed = onBackPressed
    )
}
