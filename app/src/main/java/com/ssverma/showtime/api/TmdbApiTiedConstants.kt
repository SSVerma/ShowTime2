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
        const val Stream = "stream"
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

    object AvailableCertificationTypes {
        const val U = "u"
        const val UA = "ua"
        const val A = "a"
    }

    object AvailableDiscoverOptions {
        const val language: String = "with_original_language"
        const val country: String = "country"
        const val certification = "certification"
        const val primaryReleaseDateLte = "primary_release_date.lte"
        const val primaryReleaseDateGte = "primary_release_date.gte"
        const val primaryReleaseYear = "primary_release_year"
        const val releaseType = "with_release_type"
        const val voteAvgGte = "vote_average.gte"
        const val voteAvgLte = "vote_average.lte"
        const val runtimeGte = "with_runtime.gte"
        const val runtimeLte = "with_runtime.lte"
        const val withMonetizationType = "with_watch_monetization_types"
        const val withPeople = "with_people"
        const val withGenres = "with_genres"
        const val withKeywords = "with_keywords"
        const val sortBy = "sort_by"
    }
}