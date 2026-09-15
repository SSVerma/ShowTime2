package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.ui.component.section.CreditSection

fun LazyListScope.movieCreditsSection(
    casts: List<Cast>,
    onPersonClick: (Cast) -> Unit,
    modifier: Modifier = Modifier
) {
    if (casts.isNotEmpty()) {
        item(key = "movie_credits", contentType = "credits") {
            CreditSection(
                casts = casts,
                onPersonClick = onPersonClick,
                source = "movie_credit",
                modifier = modifier
            )
        }
    }
}
