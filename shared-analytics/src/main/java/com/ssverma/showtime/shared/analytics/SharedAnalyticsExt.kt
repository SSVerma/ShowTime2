package com.ssverma.showtime.shared.analytics

import com.ssverma.shared.domain.model.MediaType

fun MediaType.asAnalyticsValue(): String = when (this) {
    MediaType.Movie -> SharedAnalyticsValues.MEDIA_TYPE_MOVIE
    MediaType.Tv -> SharedAnalyticsValues.MEDIA_TYPE_TV
    MediaType.Person -> SharedAnalyticsValues.MEDIA_TYPE_PERSON
    MediaType.Unknown -> SharedAnalyticsValues.MEDIA_TYPE_UNKNOWN
}
