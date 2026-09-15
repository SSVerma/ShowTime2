package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.ui.component.section.TagsSection

fun LazyListScope.movieKeywordsSection(
    keywords: List<Keyword>,
    onKeywordClick: (Keyword) -> Unit,
    modifier: Modifier = Modifier
) {
    if (keywords.isNotEmpty()) {
        item(key = "movie_keywords", contentType = "keywords") {
            TagsSection(
                keywords = keywords,
                onClick = onKeywordClick,
                modifier = modifier
            )
        }
    }
}
