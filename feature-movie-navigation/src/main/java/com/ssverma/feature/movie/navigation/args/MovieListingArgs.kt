package com.ssverma.feature.movie.navigation.args

import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieListingArgs(
    @param:MovieListingType
    val listingType: Int,
    @param:StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genreId: Int = 0,
    val keywordId: Int = 0
) : Parcelable

object MovieListingAvailableTypes {
    const val Default = 0
    const val TrendingToday = 1
    const val Popular = 2
    const val TopRated = 3
    const val NowInCinemas = 4
    const val Upcoming = 5
    const val Genre = 6
    const val Keyword = 7
}

@IntDef(
    MovieListingAvailableTypes.Default,
    MovieListingAvailableTypes.TrendingToday,
    MovieListingAvailableTypes.Popular,
    MovieListingAvailableTypes.TopRated,
    MovieListingAvailableTypes.NowInCinemas,
    MovieListingAvailableTypes.Upcoming,
    MovieListingAvailableTypes.Genre,
    MovieListingAvailableTypes.Keyword,
)
annotation class MovieListingType
