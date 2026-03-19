package com.ssverma.feature.tv.domain.model

import com.ssverma.feature.tv.domain.defaults.TvShowDefaults
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.TvDiscoverConfig

sealed interface TvShowListingConfig {
    data class TrendingToday(
        val timeWindow: TimeWindow = TimeWindow.Daily
    ) : TvShowListingConfig


    sealed interface Filterable : TvShowListingConfig {
        val discoverConfig: TvDiscoverConfig
        val filterConfig: TvDiscoverConfig?

        fun withFilter(filter: TvDiscoverConfig): Filterable

        data class ByGenre(
            val genreId: Int,
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig = TvDiscoverConfig.builder()
                .with(DiscoverOption.Genre(genreId = genreId))
                .build()

            override fun withFilter(filter: TvDiscoverConfig): ByGenre {
                return copy(filterConfig = filter)
            }
        }

        data class ByKeyword(
            val keywordId: Int,
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig = TvDiscoverConfig.builder()
                .with(DiscoverOption.Keyword(keywordId = keywordId))
                .build()

            override fun withFilter(filter: TvDiscoverConfig): ByKeyword {
                return copy(filterConfig = filter)
            }
        }

        data class Popular(
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig =
                TvShowDefaults.DiscoverDefaults.popular()

            override fun withFilter(filter: TvDiscoverConfig): Popular {
                return copy(filterConfig = filter)
            }
        }

        data class NowAiring(
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig =
                TvShowDefaults.DiscoverDefaults.nowAiring()

            override fun withFilter(filter: TvDiscoverConfig): NowAiring {
                return copy(filterConfig = filter)
            }
        }

        data class TodayAiring(
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig =
                TvShowDefaults.DiscoverDefaults.todayAiring()

            override fun withFilter(filter: TvDiscoverConfig): TodayAiring {
                return copy(filterConfig = filter)
            }
        }

        data class Upcoming(
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig =
                TvShowDefaults.DiscoverDefaults.upcoming()

            override fun withFilter(filter: TvDiscoverConfig): Upcoming {
                return copy(filterConfig = filter)
            }
        }

        data class TopRated(
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override val discoverConfig: TvDiscoverConfig =
                TvShowDefaults.DiscoverDefaults.topRated()

            override fun withFilter(filter: TvDiscoverConfig): TopRated {
                return copy(filterConfig = filter)
            }
        }

        data class Discovery(
            override val discoverConfig: TvDiscoverConfig,
            override val filterConfig: TvDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: TvDiscoverConfig): Discovery {
                return copy(filterConfig = filter)
            }
        }
    }
}
