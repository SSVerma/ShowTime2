package com.ssverma.feature.tv.navigation.args

import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class TvShowListingArgs(
    @TvShowListingType
    val listingType: Int,
    @StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genreId: Int = 0,
    val keywordId: Int = 0
) : Parcelable

object TvShowListingAvailableTypes {
    const val TrendingToday = 1
    const val Popular = 2
    const val TopRated = 3
    const val NowAiring = 4
    const val TodayAiring = 5
    const val Upcoming = 6
    const val Genre = 7
    const val Keyword = 8
}

@IntDef(
    TvShowListingAvailableTypes.TrendingToday,
    TvShowListingAvailableTypes.Popular,
    TvShowListingAvailableTypes.TopRated,
    TvShowListingAvailableTypes.NowAiring,
    TvShowListingAvailableTypes.TodayAiring,
    TvShowListingAvailableTypes.Upcoming,
    TvShowListingAvailableTypes.Genre,
    TvShowListingAvailableTypes.Keyword,
)
annotation class TvShowListingType
