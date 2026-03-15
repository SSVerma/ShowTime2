package com.ssverma.feature.movie.domain.model

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.feature.movie.domain.defaults.MovieDefaults

sealed interface MovieListingConfig {
    data class TrendingToday(
        val timeWindow: TimeWindow = TimeWindow.Daily,
        val watchRegion: String? = null
    ) : MovieListingConfig


    sealed interface Filterable : MovieListingConfig {
        val discoverConfig: MovieDiscoverConfig
        val filterConfig: MovieDiscoverConfig?

        fun withFilter(filter: MovieDiscoverConfig): Filterable

        data class ByGenre(
            val genreId: Int,
            override val discoverConfig: MovieDiscoverConfig = MovieDiscoverConfig.builder()
                .with(
                    MovieDefaults.DefaultMovieReleaseType,
                    DiscoverOption.Genre(genreId = genreId)
                )
                .build(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): ByGenre {
                return copy(filterConfig = filter)
            }
        }

        data class ByKeyword(
            val keywordId: Int,
            override val discoverConfig: MovieDiscoverConfig = MovieDiscoverConfig.builder()
                .with(
                    MovieDefaults.DefaultMovieReleaseType,
                    DiscoverOption.Keyword(keywordId = keywordId)
                )
                .build(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): ByKeyword {
                return copy(filterConfig = filter)
            }
        }

        data class Popular(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.popular(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): Popular {
                return copy(filterConfig = filter)
            }
        }

        data class NowInCinemas(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.inCinemas(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): NowInCinemas {
                return copy(filterConfig = filter)
            }
        }

        data class Upcoming(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.upcoming(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): Upcoming {
                return copy(filterConfig = filter)
            }
        }

        data class TopRated(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.topRated(),
            override val filterConfig: MovieDiscoverConfig? = null
        ) : Filterable {
            override fun withFilter(filter: MovieDiscoverConfig): TopRated {
                return copy(filterConfig = filter)
            }
        }
    }
}