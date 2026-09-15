package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.ui.component.section.MediaReactionsSection

fun LazyListScope.tvReactionsSection(
    mediaReactions: MediaReactions,
    onReactionTagClicked: (MediaReactionTag) -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "tv_reactions", contentType = "reactions") {
        MediaReactionsSection(
            reactions = mediaReactions,
            onTagClick = onReactionTagClicked,
            modifier = modifier
        )
    }
}
