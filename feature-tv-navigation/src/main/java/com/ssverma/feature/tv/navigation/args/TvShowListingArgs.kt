package com.ssverma.feature.tv.navigation.args

import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class TvShowListingArgs(
    @param:TvShowListingType
    val listingType: Int,
    @param:StringRes val titleRes: Int = 0,
    val title: String? = null,
    val genreId: Int = 0,
    val keywordId: Int = 0,
    val watchProviderId: Int = 0,
    val watchRegion: String? = null
) : Parcelable

object TvShowListingAvailableTypes {
    const val Default = 0
    const val TrendingToday = 1
    const val Popular = 2
    const val TopRated = 3
    const val NowAiring = 4
    const val TodayAiring = 5
    const val Upcoming = 6
    const val Genre = 7
    const val Keyword = 8
    const val WatchProvider = 9
    const val Discovery = 10
    const val WatchProviderNew = 11
    const val WatchProviderUpcoming = 12
    const val WatchProviderTopRated = 13
}

@IntDef(
    TvShowListingAvailableTypes.Default,
    TvShowListingAvailableTypes.TrendingToday,
    TvShowListingAvailableTypes.Popular,
    TvShowListingAvailableTypes.TopRated,
    TvShowListingAvailableTypes.NowAiring,
    TvShowListingAvailableTypes.TodayAiring,
    TvShowListingAvailableTypes.Upcoming,
    TvShowListingAvailableTypes.Genre,
    TvShowListingAvailableTypes.Keyword,
    TvShowListingAvailableTypes.WatchProvider,
    TvShowListingAvailableTypes.Discovery,
    TvShowListingAvailableTypes.WatchProviderNew,
    TvShowListingAvailableTypes.WatchProviderUpcoming,
    TvShowListingAvailableTypes.WatchProviderTopRated,
)
annotation class TvShowListingType
