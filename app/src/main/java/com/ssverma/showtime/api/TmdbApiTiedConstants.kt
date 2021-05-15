package com.ssverma.showtime.api

const val TMDB_API_PAGE_SIZE = 20
const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500" //TODO: Fetch from configs

object TmdbApiTiedConstants {

    object AvailableMediaTypes {
        const val ALL = "all"
        const val MOVIE = "movie"
        const val TV = "tv"
        const val PERSON = "person"
    }

    object AvailableTimeWindows {
        const val DAY = "day"
        const val WEEK = "week"
    }

    object AvailableMonetizationTypes {
        const val Free = "free"
        const val Ads = "ads"
        const val Rent = "rent"
        const val Buy = "buy"
    }

    object AvailableReleaseTypes {
        const val Premiere = 1
        const val TheatricalLimited = 2
        const val Theatrical = 3
        const val Digital = 4
        const val Physical = 5
        const val Tv = 6
    }

    object AvailableSortingOptions {
        const val PopularityAsc = "popularity.asc"
        const val PopularityDesc = "popularity.desc"
        const val ReleaseDateAsc = "release_date.asc"
        const val ReleaseDateDesc = "release_date.desc"
        const val RevenueAsc = "revenue.asc"
        const val RevenueDesc = "revenue.desc"
        const val PrimaryReleaseDateAsc = "primary_release_date.asc"
        const val PrimaryReleaseDateDesc = "primary_release_date.desc"
        const val OriginalTitleAsc = "original_title.asc"
        const val OriginalTitleDesc = "original_title.desc"
        const val VoteAvgAsc = "vote_average.asc"
        const val VoteAvgDesc = "vote_average.desc"
        const val VoteCountAsc = "vote_count.asc"
        const val VoteCountDesc = "vote_count.desc"
    }
}