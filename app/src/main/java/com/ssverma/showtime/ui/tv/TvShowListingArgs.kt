package com.ssverma.showtime.ui.tv

import android.os.Parcelable
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.ssverma.showtime.domain.model.tv.TvShowListingConfig
import kotlinx.android.parcel.Parcelize

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
