package com.ssverma.feature.tv.analytics

import com.ssverma.feature.tv.domain.model.TvShowListingConfig

fun TvShowListingConfig.asAnalyticsListingType(): String {
    return when (this) {
        is TvShowListingConfig.Filterable.ByGenre -> "genre ${this.genreId}"
        is TvShowListingConfig.Filterable.ByKeyword -> "keyword ${this.keywordId}"
        is TvShowListingConfig.Filterable.Discovery -> "discovery"
        is TvShowListingConfig.Filterable.NowAiring -> "now_airing"
        is TvShowListingConfig.Filterable.Popular -> "popular"
        is TvShowListingConfig.Filterable.TodayAiring -> "today_airing"
        is TvShowListingConfig.Filterable.TopRated -> "top_rated"
        is TvShowListingConfig.Filterable.Upcoming -> "upcoming"
        is TvShowListingConfig.TrendingToday -> "trending_today"
    }
}
