package com.ssverma.feature.tv.ui.list.component

import androidx.compose.runtime.Composable
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.TrendingBadge

@Composable
fun TvIndicator(type: Int, tvShow: TvShowPreview) {
    when (type) {
        TvShowListingAvailableTypes.Popular -> TrendingBadge(popularityText = tvShow.displayPopularity)
        TvShowListingAvailableTypes.TopRated -> ScoreBadge(score = tvShow.voteAvgPercentage)
        TvShowListingAvailableTypes.Upcoming -> tvShow.displayFirstAirDate?.let { DateBadge(dateText = it) }
        TvShowListingAvailableTypes.NowAiring -> tvShow.displayFirstAirDate?.let {
            DateBadge(dateText = it)
        }
        TvShowListingAvailableTypes.TodayAiring -> tvShow.displayFirstAirDate?.let {
            DateBadge(dateText = it)
        }
    }
}
