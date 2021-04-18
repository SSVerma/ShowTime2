package com.ssverma.showtime.api

object DiscoverMovieQueryMap {
    fun of(
        primaryReleaseDateLte: String? = null,
        monetizationType: String? = null
    ): Map<String, String> {
        return mapOf(
            Pair(first = "primary_release_date.lte", second = primaryReleaseDateLte ?: ""),
            Pair(first = "with_watch_monetization_types", second = monetizationType ?: "")
        )
    }
}