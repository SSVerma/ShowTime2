package com.ssverma.feature.tv.navigation.convertor

import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes

fun TvShowListingArgs.asTvShowListingConfigs(): TvShowListingConfig {
    return when (this.listingType) {
        TvShowListingAvailableTypes.TrendingToday -> {
            TvShowListingConfig.TrendingToday()
        }
        TvShowListingAvailableTypes.Popular -> {
            TvShowListingConfig.Filterable.Popular()
        }
        TvShowListingAvailableTypes.TopRated -> {
            TvShowListingConfig.TopRated
        }
        TvShowListingAvailableTypes.NowAiring -> {
            TvShowListingConfig.Filterable.NowAiring()
        }
        TvShowListingAvailableTypes.TodayAiring -> {
            TvShowListingConfig.Filterable.TodayAiring()
        }
        TvShowListingAvailableTypes.Upcoming -> {
            TvShowListingConfig.Filterable.Upcoming()
        }
        TvShowListingAvailableTypes.Genre -> {
            TvShowListingConfig.Filterable.ByGenre(genreId = genreId)
        }
        TvShowListingAvailableTypes.Keyword -> {
            TvShowListingConfig.Filterable.ByKeyword(keywordId = keywordId)
        }
        else -> {
            // Update with fallback, instead of crashing the app
            throw IllegalArgumentException("Invalid listing type: $listingType")
        }
    }
}