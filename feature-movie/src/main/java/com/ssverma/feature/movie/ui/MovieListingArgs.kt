package com.ssverma.feature.movie.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.navigation.NavType
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieListingArgs(
    @MovieListingType
    val listingType: Int,
    @StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genreId: Int = 0,
    val keywordId: Int = 0
) : Parcelable

object MovieListingAvailableTypes {
    const val TrendingToday = 1
    const val Popular = 2
    const val TopRated = 3
    const val NowInCinemas = 4
    const val Upcoming = 5
    const val Genre = 6
    const val Keyword = 7
}

@IntDef(
    MovieListingAvailableTypes.TrendingToday,
    MovieListingAvailableTypes.Popular,
    MovieListingAvailableTypes.TopRated,
    MovieListingAvailableTypes.NowInCinemas,
    MovieListingAvailableTypes.Upcoming,
    MovieListingAvailableTypes.Genre,
    MovieListingAvailableTypes.Keyword,
)
annotation class MovieListingType


class MovieListingNavType : NavType<MovieListingArgs>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): MovieListingArgs? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): MovieListingArgs {
        return Gson().fromJson(value, MovieListingArgs::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: MovieListingArgs) {
        bundle.putParcelable(key, value)
    }
}

fun MovieListingArgs.asMovieListingConfigs(): com.ssverma.feature.movie.domain.model.MovieListingConfig {
    return when (this.listingType) {
        MovieListingAvailableTypes.TrendingToday -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.TrendingToday()
        }
        MovieListingAvailableTypes.Popular -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.Filterable.Popular()
        }
        MovieListingAvailableTypes.TopRated -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.TopRated
        }
        MovieListingAvailableTypes.NowInCinemas -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.Filterable.NowInCinemas()
        }
        MovieListingAvailableTypes.Upcoming -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.Filterable.Upcoming()
        }
        MovieListingAvailableTypes.Genre -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.Filterable.ByGenre(genreId = genreId)
        }
        MovieListingAvailableTypes.Keyword -> {
            com.ssverma.feature.movie.domain.model.MovieListingConfig.Filterable.ByKeyword(keywordId = keywordId)
        }
        else -> {
            // Update with fallback, instead of crashing the app
            throw IllegalArgumentException("Invalid listing type: $listingType")
        }
    }
}
