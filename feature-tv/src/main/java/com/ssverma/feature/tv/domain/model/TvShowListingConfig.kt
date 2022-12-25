package com.ssverma.feature.tv.domain.model

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.feature.tv.domain.defaults.TvShowDefaults

sealed interface TvShowListingConfig {
    data class TrendingToday(
        val timeWindow: TimeWindow = TimeWindow.Daily
    ) : TvShowListingConfig

    object TopRated : TvShowListingConfig

    sealed interface Filterable : TvShowListingConfig {
        val discoverConfig: TvDiscoverConfig
        var filterConfig: TvDiscoverConfig?

        data class ByGenre(
            val genreId: Int,
            override val discoverConfig: TvDiscoverConfig = TvDiscoverConfig.builder()
                .with(DiscoverOption.Genre(genreId = genreId))
                .build(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable

        data class ByKeyword(
            val keywordId: Int,
            override val discoverConfig: TvDiscoverConfig = TvDiscoverConfig.builder()
                .with(DiscoverOption.Keyword(keywordId = keywordId))
                .build(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable

        data class Popular(
            override val discoverConfig: TvDiscoverConfig = TvShowDefaults.DiscoverDefaults.popular(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable

        data class NowAiring(
            override val discoverConfig: TvDiscoverConfig = TvShowDefaults.DiscoverDefaults.nowAiring(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable

        data class TodayAiring(
            override val discoverConfig: TvDiscoverConfig = TvShowDefaults.DiscoverDefaults.todayAiring(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable

        data class Upcoming(
            override val discoverConfig: TvDiscoverConfig = TvShowDefaults.DiscoverDefaults.upcoming(),
            override var filterConfig: TvDiscoverConfig? = null
        ) : Filterable
    }
}