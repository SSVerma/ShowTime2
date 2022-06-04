package com.ssverma.feature.movie.domain.model

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.feature.movie.domain.defaults.MovieDefaults

sealed interface MovieListingConfig {
    data class TrendingToday(
        val timeWindow: TimeWindow = TimeWindow.Daily
    ) : MovieListingConfig

    object TopRated : MovieListingConfig

    sealed interface Filterable : MovieListingConfig {
        val discoverConfig: MovieDiscoverConfig
        var filterConfig: MovieDiscoverConfig?

        data class ByGenre(
            val genreId: Int,
            override val discoverConfig: MovieDiscoverConfig = MovieDiscoverConfig.builder()
                .with(
                    MovieDefaults.DefaultMovieReleaseType,
                    DiscoverOption.Genre(genreId = genreId)
                )
                .build(),
            override var filterConfig: MovieDiscoverConfig? = null
        ) : Filterable

        data class ByKeyword(
            val keywordId: Int,
            override val discoverConfig: MovieDiscoverConfig = MovieDiscoverConfig.builder()
                .with(
                    MovieDefaults.DefaultMovieReleaseType,
                    DiscoverOption.Keyword(keywordId = keywordId)
                )
                .build(),
            override var filterConfig: MovieDiscoverConfig? = null
        ) : Filterable

        data class Popular(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.popular(),
            override var filterConfig: MovieDiscoverConfig? = null
        ) : Filterable

        data class NowInCinemas(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.inCinemas(),
            override var filterConfig: MovieDiscoverConfig? = null
        ) : Filterable

        data class Upcoming(
            override val discoverConfig: MovieDiscoverConfig = MovieDefaults.DiscoverDefaults.upcoming(),
            override var filterConfig: MovieDiscoverConfig? = null
        ) : Filterable
    }
}