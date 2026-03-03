package com.ssverma.feature.movie.ui.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.scrim
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.AppHeroCarousel
import com.ssverma.shared.ui.component.HomePageAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroSection(
    trendingMoviesState: UiState<List<MoviePreview>, MovieFailure>,
    onSearchClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onMovieClicked: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    DriveCompose(
        uiState = trendingMoviesState,
        onRetry = onRetry
    ) { movies ->
        // Carousel state management
        val carouselState = rememberCarouselState { movies.size }

        // Dynamically derive the backdrop based on current carousel index
        val currentBackdrop by remember {
            derivedStateOf { movies.getOrNull(carouselState.currentItem)?.backdropImageUrl }
        }

        val scrimColor = MaterialTheme.colorScheme.background

        Box(modifier = modifier) {
            // Background Backdrop with Scrim
            currentBackdrop?.let { url ->
                NetworkImage(
                    url = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .scrim(
                            colors = listOf(
                                scrimColor.copy(alpha = 0.4f),
                                scrimColor.copy(alpha = 1f)
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                // Transparent Top Bar for that cinematic overlap
                HomePageAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    onSearchIconPressed = onSearchClicked,
                    onAccountIconPressed = onAccountClicked
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // The Hero Carousel - Mapping items to the generic component
                AppHeroCarousel(
                    items = movies,
                    carouselState = carouselState,
                    imageUrl = { it.posterImageUrl },
                    title = { it.title },
                    onItemClick = { onMovieClicked(it.id) }
                )
            }
        }
    }
}
