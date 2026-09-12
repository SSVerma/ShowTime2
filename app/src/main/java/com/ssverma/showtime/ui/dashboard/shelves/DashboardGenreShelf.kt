package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre

private object DashboardGenreDefaults {
    val GridHeight = 88.dp
    val ChipHeight = 34.dp

    val ShimmerWidths = listOf(
        80.dp, 110.dp, 65.dp, 100.dp, 75.dp,
        120.dp, 90.dp, 85.dp, 105.dp, 70.dp
    )
}

fun LazyListScope.dashboardGenreShelf(
    movieGenres: UiState<List<Genre>, Failure.CoreFailure>,
    tvGenres: UiState<List<Genre>, Failure.CoreFailure>,
    isMovieSelected: Boolean,
    onToggleGenreType: (Boolean) -> Unit,
    onGenreClicked: (Genre, Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "dashboard_genre_shelf") {
        DashboardGenreShelf(
            movieGenres = movieGenres,
            tvGenres = tvGenres,
            isMovieSelected = isMovieSelected,
            onToggleGenreType = onToggleGenreType,
            onGenreClicked = onGenreClicked,
            onRetry = onRetry,
            modifier = modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DashboardGenreShelf(
    movieGenres: UiState<List<Genre>, Failure.CoreFailure>,
    tvGenres: UiState<List<Genre>, Failure.CoreFailure>,
    isMovieSelected: Boolean,
    onToggleGenreType: (Boolean) -> Unit,
    onGenreClicked: (Genre, Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentGenresState = if (isMovieSelected) movieGenres else tvGenres

    Column(modifier = modifier) {
        // Section Header Row with Segmented Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Explore Genres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            GenreSegmentedSwitcher(
                isMovieSelected = isMovieSelected,
                onToggle = onToggleGenreType
            )
        }

        // Staggered Grid with StatefulContent
        StatefulContent(
            state = currentGenresState,
            onRetry = onRetry,
            loading = { GenreShimmerPlaceholder() }
        ) { genres ->
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalItemSpacing = 8.dp,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DashboardGenreDefaults.GridHeight)
            ) {
                items(items = genres, key = { it.id }) { genre ->
                    AssistChip(
                        onClick = { onGenreClicked(genre, isMovieSelected) },
                        label = {
                            Text(
                                text = genre.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreSegmentedSwitcher(
    isMovieSelected: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(2.dp)
        ) {
            // Movie Option
            val movieBg by animateColorAsState(
                targetValue = if (isMovieSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                animationSpec = tween(200),
                label = "movie_bg"
            )
            val movieText by animateColorAsState(
                targetValue = if (isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "movie_text"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(movieBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onToggle(true) }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Movies",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = if (isMovieSelected) FontWeight.Bold else FontWeight.Medium,
                    color = movieText
                )
            }

            // TV Shows Option
            val tvBg by animateColorAsState(
                targetValue = if (!isMovieSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                animationSpec = tween(200),
                label = "tv_bg"
            )
            val tvText by animateColorAsState(
                targetValue = if (!isMovieSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "tv_text"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(tvBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onToggle(false) }
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "TV Shows",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = if (!isMovieSelected) FontWeight.Bold else FontWeight.Medium,
                    color = tvText
                )
            }
        }
    }
}

@Composable
private fun GenreShimmerPlaceholder() {
    LazyHorizontalStaggeredGrid(
        rows = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalItemSpacing = 8.dp,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(DashboardGenreDefaults.GridHeight)
    ) {
        items(DashboardGenreDefaults.ShimmerWidths) { width ->
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(width)
                    .height(DashboardGenreDefaults.ChipHeight),
                shape = CircleShape
            )
        }
    }
}
