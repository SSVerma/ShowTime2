package com.ssverma.showtime.domain.model.tv

import com.ssverma.showtime.domain.DiscoverOption
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.TvDiscoverConfig
import com.ssverma.showtime.domain.defaults.tv.TvShowDefaults

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