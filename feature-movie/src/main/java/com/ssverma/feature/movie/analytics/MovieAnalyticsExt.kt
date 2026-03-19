package com.ssverma.feature.movie.analytics

import com.ssverma.feature.movie.domain.model.MovieListingConfig


fun MovieListingConfig.asAnalyticsListingType(): String {
    return when (this) {
        is MovieListingConfig.Filterable.ByGenre -> "genre ${this.genreId}"
        is MovieListingConfig.Filterable.ByKeyword -> "keyword ${this.keywordId}"
        is MovieListingConfig.Filterable.Discovery -> "discovery"
        is MovieListingConfig.Filterable.NowInCinemas -> "now_in_cinemas"
        is MovieListingConfig.Filterable.Popular -> "popular"
        is MovieListingConfig.Filterable.TopRated -> "top_rated"
        is MovieListingConfig.Filterable.Upcoming -> "upcoming"
        is MovieListingConfig.TrendingToday -> "trending_today"
    }
}
