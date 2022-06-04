package com.ssverma.feature.movie.navigation.convertor

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
        MovieListingAvailableTypes.TopRated -> {
            MovieListingConfig.TopRated
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
            // Update with fallback, instead of crashing the app
            throw IllegalArgumentException("Invalid listing type: $listingType")
        }
    }
}