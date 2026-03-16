package com.ssverma.feature.tv.navigation.convertor

import com.ssverma.feature.tv.domain.defaults.TvShowDefaults
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes

fun TvShowListingArgs.asTvShowListingConfigs(): TvShowListingConfig {
    return when (this.listingType) {
        TvShowListingAvailableTypes.Default -> {
            TvShowListingConfig.Filterable.Popular()
        }

        TvShowListingAvailableTypes.TrendingToday -> {
            TvShowListingConfig.TrendingToday()
        }

        TvShowListingAvailableTypes.Popular -> {
            TvShowListingConfig.Filterable.Popular()
        }

        TvShowListingAvailableTypes.WatchProvider -> {
            TvShowListingConfig.Filterable.WatchProvider(
                watchProviderId = watchProviderId,
                watchRegion = watchRegion.orEmpty()
            )
        }

        TvShowListingAvailableTypes.Discovery -> {
            TvShowListingConfig.Filterable.Discovery(
                discoverConfig = TvShowDefaults.DiscoverDefaults.discoveryBy(
                    watchRegion = watchRegion,
                    watchProviderId = if (watchProviderId > 0) watchProviderId else null
                )
            )
        }

        TvShowListingAvailableTypes.WatchProviderNew -> {
            TvShowListingConfig.Filterable.Discovery(
                discoverConfig = TvShowDefaults.DiscoverDefaults.watchProviderNew(watchProviderId)
            )
        }

        TvShowListingAvailableTypes.WatchProviderUpcoming -> {
            TvShowListingConfig.Filterable.Discovery(
                discoverConfig = TvShowDefaults.DiscoverDefaults.watchProviderUpcoming(
                    watchProviderId = watchProviderId
                )
            )
        }

        TvShowListingAvailableTypes.WatchProviderTopRated -> {
            TvShowListingConfig.Filterable.Discovery(
                discoverConfig = TvShowDefaults.DiscoverDefaults.watchProviderTopRated(
                    watchProviderId
                )
            )
        }

        TvShowListingAvailableTypes.TopRated -> {
            TvShowListingConfig.Filterable.TopRated()
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
            TvShowListingConfig.Filterable.Popular()
        }
    }
}