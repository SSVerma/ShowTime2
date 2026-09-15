package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.ui.component.section.OverviewSection

fun LazyListScope.movieOverviewSection(
    overview: String,
    modifier: Modifier = Modifier
) {
    if (overview.isNotBlank()) {
        item(key = "movie_overview", contentType = "overview") {
            OverviewSection(
                overview = overview,
                modifier = modifier
            )
        }
    }
}
