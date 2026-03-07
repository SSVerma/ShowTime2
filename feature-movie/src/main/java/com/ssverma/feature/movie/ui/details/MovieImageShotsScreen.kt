package com.ssverma.feature.movie.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.shared.ui.bottomsheet.ImageShotsListScreen

@Composable
fun MovieImageShotsScreen(
    navController: NavController,
    onBackPressed: () -> Unit,
    openImagePager: (Int) -> Unit,
    viewModel: MovieDetailsViewModel = navController.destinationViewModel(destination = MovieDetailDestination)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ImageShotsListScreen(
        imageShots = uiState.imageShots,
        onBackPressed = onBackPressed,
        openImagePager = openImagePager
    )
}
