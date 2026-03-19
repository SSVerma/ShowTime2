package com.ssverma.feature.movie.ui.list.component

import androidx.compose.runtime.Composable
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.ui.component.media.DateBadge
import com.ssverma.shared.ui.component.media.ScoreBadge
import com.ssverma.shared.ui.component.media.TrendingBadge

@Composable
fun MovieIndicator(config: MovieListingConfig, movie: MoviePreview) {
    when (config) {
        is MovieListingConfig.Filterable.Popular -> TrendingBadge(popularityText = movie.displayPopularity)
        is MovieListingConfig.Filterable.TopRated -> ScoreBadge(score = movie.voteAvgPercentage)
        is MovieListingConfig.Filterable.Upcoming -> movie.displayReleaseDate?.let {
            DateBadge(dateText = it)
        }

        is MovieListingConfig.Filterable.NowInCinemas -> movie.displayReleaseDate?.let {
            DateBadge(dateText = it)
        }

        else -> {
            // Fallback: Show nothing
        }
    }
}
