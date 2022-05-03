package com.ssverma.showtime.ui.movie

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.navigation.NavType
import com.google.gson.Gson
import com.ssverma.showtime.domain.model.movie.MovieListingConfig
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
