package com.ssverma.feature.movie.ui.list.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.ui.paging.PagedGrid
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.ui.list.component.MovieIndicator
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.MovieGridItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoviesGridContent(
    moviePagingItems: LazyPagingItems<MoviePreview>,
    type: Int,
    openMovieDetails: (movieId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PagedGrid(
        pagingItems = moviePagingItems,
        contentPadding = PaddingValues(MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        modifier = modifier
    ) { movie ->
        MovieGridItem(
            movie = movie,
            indicator = { preview -> MovieIndicator(type = type, movie = preview) },
            onClick = { preview -> openMovieDetails(preview.id) },
            posterModifier = Modifier.fillMaxWidth()
        )
    }
}
