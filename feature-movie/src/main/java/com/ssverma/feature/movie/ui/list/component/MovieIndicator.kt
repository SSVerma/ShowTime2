package com.ssverma.feature.movie.ui.list.component

import androidx.compose.runtime.Composable
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.TrendingBadge

@Composable
fun MovieIndicator(type: Int, movie: MoviePreview) {
    when (type) {
        MovieListingAvailableTypes.Popular -> TrendingBadge(popularityText = movie.displayPopularity)
        MovieListingAvailableTypes.TopRated -> ScoreBadge(score = movie.voteAvgPercentage)
        MovieListingAvailableTypes.Upcoming -> movie.displayReleaseDate?.let { DateBadge(dateText = it) }
        MovieListingAvailableTypes.NowInCinemas -> movie.displayReleaseDate?.let {
            DateBadge(dateText = it)
        }
    }
}
