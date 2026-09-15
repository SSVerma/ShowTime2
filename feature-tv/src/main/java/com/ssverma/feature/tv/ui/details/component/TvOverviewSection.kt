package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.ui.component.section.OverviewSection

fun LazyListScope.tvOverviewSection(
    overview: String,
    modifier: Modifier = Modifier
) {
    if (overview.isNotBlank()) {
        item(key = "tv_overview", contentType = "overview") {
            OverviewSection(
                overview = overview,
                modifier = modifier
            )
        }
    }
}
