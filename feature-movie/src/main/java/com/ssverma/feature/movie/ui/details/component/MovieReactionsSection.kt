package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.ui.component.section.MediaReactionsSection

fun LazyListScope.movieReactionsSection(
    mediaReactions: MediaReactions,
    onReactionTagClicked: (MediaReactionTag) -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "movie_reactions", contentType = "reactions") {
        MediaReactionsSection(
            reactions = mediaReactions,
            onTagClick = onReactionTagClicked,
            modifier = modifier
        )
    }
}
