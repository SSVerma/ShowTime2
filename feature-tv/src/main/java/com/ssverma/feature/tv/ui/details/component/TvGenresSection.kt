package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.ui.component.GenreItem

fun LazyListScope.tvGenresSection(
    genres: List<Genre>,
    onGenreClicked: (Genre) -> Unit,
    modifier: Modifier = Modifier
) {
    if (genres.isNotEmpty()) {
        item(key = "tv_genres", contentType = "genres") {
            Box(modifier = modifier) {
                HorizontalLazyList(
                    items = genres,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
                ) { genre ->
                    GenreItem(genre = genre) {
                        onGenreClicked(genre)
                    }
                }
            }
        }
    }
}
