package com.ssverma.feature.movie.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.StatefulContent
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre

object GenreDefaults {
    val GridHeight = 88.dp
    val ChipHeight = 32.dp

    val ShimmerWidths = listOf(
        80.dp, 110.dp, 60.dp, 100.dp, 70.dp,
        120.dp, 90.dp, 85.dp, 105.dp, 75.dp
    )
}

@Composable
fun MovieGenres(
    genresUiState: UiState<List<Genre>, Failure.CoreFailure>,
    onGenreClicked: (Genre) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    StatefulContent(
        state = genresUiState,
        onRetry = onRetry,
        loading = { GenreShimmerPlaceholder(modifier) }
    ) { genres ->

        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
            horizontalItemSpacing = MaterialTheme.spacing.small,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = modifier
                .fillMaxWidth()
                .height(GenreDefaults.GridHeight)
        ) {
            items(items = genres, key = { it.id }) { genre ->
                AssistChip(
                    onClick = { onGenreClicked(genre) },
                    label = {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun GenreShimmerPlaceholder(modifier: Modifier = Modifier) {
    LazyHorizontalStaggeredGrid(
        rows = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
        horizontalItemSpacing = MaterialTheme.spacing.small,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
            .fillMaxWidth()
            .height(GenreDefaults.GridHeight)
    ) {
        items(GenreDefaults.ShimmerWidths) { width ->
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(width)
                    .height(GenreDefaults.ChipHeight),
                shape = MaterialTheme.shapes.large
            )
        }
    }
}
