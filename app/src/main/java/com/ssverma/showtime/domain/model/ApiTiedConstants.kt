package com.ssverma.showtime.domain.model

object ApiTiedConstants {

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
}