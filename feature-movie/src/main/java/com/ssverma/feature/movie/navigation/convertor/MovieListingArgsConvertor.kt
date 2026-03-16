package com.ssverma.feature.movie.navigation.convertor

import com.ssverma.feature.movie.domain.defaults.MovieDefaults
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes

fun MovieListingArgs.asMovieListingConfigs(): MovieListingConfig {
    return when (this.listingType) {
        MovieListingAvailableTypes.TrendingToday -> {
            MovieListingConfig.TrendingToday()
        }
        MovieListingAvailableTypes.Popular -> {
            MovieListingConfig.Filterable.Popular()
        }
        MovieListingAvailableTypes.Default -> {
            MovieListingConfig.Filterable.Popular()
        }
        MovieListingAvailableTypes.WatchProvider -> {
            MovieListingConfig.Filterable.WatchProvider(watchProviderId = watchProviderId)
        }
        MovieListingAvailableTypes.Discovery -> {
            MovieListingConfig.Filterable.Discovery(
                discoverConfig = MovieDefaults.DiscoverDefaults.discoveryBy(
                    watchRegion = watchRegion,
                    watchProviderId = if (watchProviderId > 0) watchProviderId else null
                )
            )
        }
        MovieListingAvailableTypes.WatchProviderNew -> {
            MovieListingConfig.Filterable.Discovery(
                discoverConfig = MovieDefaults.DiscoverDefaults.watchProviderNew(watchProviderId)
            )
        }
        MovieListingAvailableTypes.WatchProviderUpcoming -> {
            MovieListingConfig.Filterable.Discovery(
                discoverConfig = MovieDefaults.DiscoverDefaults.watchProviderUpcoming(
                    watchProviderId
                )
            )
        }
        MovieListingAvailableTypes.WatchProviderTopRated -> {
            MovieListingConfig.Filterable.Discovery(
                discoverConfig = MovieDefaults.DiscoverDefaults.watchProviderTopRated(
                    watchProviderId
                )
            )
        }
        MovieListingAvailableTypes.TopRated -> {
            MovieListingConfig.Filterable.TopRated()
        }
        MovieListingAvailableTypes.NowInCinemas -> {
            MovieListingConfig.Filterable.NowInCinemas()
        }
        MovieListingAvailableTypes.Upcoming -> {
            MovieListingConfig.Filterable.Upcoming()
        }
        MovieListingAvailableTypes.Genre -> {
            MovieListingConfig.Filterable.ByGenre(genreId = genreId)
        }
        MovieListingAvailableTypes.Keyword -> {
            MovieListingConfig.Filterable.ByKeyword(keywordId = keywordId)
        }
        else -> {
            MovieListingConfig.Filterable.Popular()
        }
    }
}