package com.ssverma.feature.tv.ui.list.component

import androidx.compose.runtime.Composable
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.TrendingBadge

@Composable
fun TvIndicator(config: TvShowListingConfig, tvShow: TvShowPreview) {
    when (config) {
        is TvShowListingConfig.Filterable.Popular -> TrendingBadge(popularityText = tvShow.displayPopularity)
        is TvShowListingConfig.Filterable.TopRated -> ScoreBadge(score = tvShow.voteAvgPercentage)
        else -> {
            // No indicator for other types
        }
    }
}
