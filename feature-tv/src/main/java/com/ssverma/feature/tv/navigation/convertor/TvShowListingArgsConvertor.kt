package com.ssverma.feature.tv.navigation.convertor

import com.ssverma.feature.tv.domain.defaults.TvShowDefaults
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs

fun TvShowListingArgs.asTvShowListingConfigs(): TvShowListingConfig {
    return when (this) {
        is TvShowListingArgs.TrendingToday -> {
            TvShowListingConfig.TrendingToday()
        }

        is TvShowListingArgs.Popular -> {
            TvShowListingConfig.Filterable.Popular()
        }

        is TvShowListingArgs.TopRated -> {
            TvShowListingConfig.Filterable.TopRated()
        }

        is TvShowListingArgs.NowAiring -> {
            TvShowListingConfig.Filterable.NowAiring()
        }

        is TvShowListingArgs.TodayAiring -> {
            TvShowListingConfig.Filterable.TodayAiring()
        }

        is TvShowListingArgs.Upcoming -> {
            TvShowListingConfig.Filterable.Upcoming()
        }

        is TvShowListingArgs.ByGenre -> {
            TvShowListingConfig.Filterable.ByGenre(genreId = genreId)
        }

        is TvShowListingArgs.ByKeyword -> {
            TvShowListingConfig.Filterable.ByKeyword(keywordId = keywordId)
        }

        is TvShowListingArgs.Discovery -> {
            TvShowListingConfig.Filterable.Discovery(discoverConfig = initialConfig)
        }
    }
}