package com.ssverma.feature.movie.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre

@Composable
fun MovieGenres(
    genresUiState: UiState<List<Genre>, Failure.CoreFailure>,
    onGenreClicked: (Genre) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    DriveCompose(
        uiState = genresUiState,
        onRetry = onRetry,
        loading = { GenreShimmerPlaceholder() }
    ) { genres ->
        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = modifier,
        ) {
            items(genres) { genre ->
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
private fun GenreShimmerPlaceholder() {
    LazyRow(
        modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        repeat(5) {
            item {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }
}
