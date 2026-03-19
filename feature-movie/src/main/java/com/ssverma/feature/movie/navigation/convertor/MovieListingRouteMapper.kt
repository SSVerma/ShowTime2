package com.ssverma.feature.movie.navigation.convertor

import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.feature.movie.navigation.args.MovieListingArgs

fun MovieListingArgs.asMovieListingConfig(): MovieListingConfig {
    return when (this) {
        is MovieListingArgs.TrendingToday -> MovieListingConfig.TrendingToday()
        is MovieListingArgs.Popular -> MovieListingConfig.Filterable.Popular()
        is MovieListingArgs.TopRated -> MovieListingConfig.Filterable.TopRated()
        is MovieListingArgs.NowInCinemas -> MovieListingConfig.Filterable.NowInCinemas()
        is MovieListingArgs.Upcoming -> MovieListingConfig.Filterable.Upcoming()
        is MovieListingArgs.ByGenre -> MovieListingConfig.Filterable.ByGenre(genreId = this.genreId)
        is MovieListingArgs.ByKeyword -> MovieListingConfig.Filterable.ByKeyword(keywordId = this.keywordId)
        is MovieListingArgs.Discovery -> MovieListingConfig.Filterable.Discovery(discoverConfig = this.initialConfig)
    }
}
