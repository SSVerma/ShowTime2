package com.ssverma.feature.movie.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ssverma.core.navigation.destinationViewModel
import com.ssverma.feature.movie.navigation.MovieDetailDestination
import com.ssverma.shared.ui.bottomsheet.ImagePagerScreen

@Composable
fun MovieImagePagerScreen(
    navController: NavController,
    defaultPageIndex: Int,
    onBackPressed: () -> Unit,
    viewModel: MovieDetailsViewModel = navController.destinationViewModel(destination = MovieDetailDestination)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ImagePagerScreen(
        imageShots = uiState.imageShots,
        defaultPageIndex = defaultPageIndex,
        onBackPressed = onBackPressed
    )
}
