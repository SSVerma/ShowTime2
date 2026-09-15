package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.ui.component.section.CreditSection

fun LazyListScope.tvCreditsSection(
    casts: List<Cast>,
    onPersonClick: (Cast) -> Unit,
    modifier: Modifier = Modifier
) {
    if (casts.isNotEmpty()) {
        item(key = "tv_credits", contentType = "credits") {
            CreditSection(
                casts = casts,
                onPersonClick = onPersonClick,
                source = "tv_show_credit",
                modifier = modifier
            )
        }
    }
}
