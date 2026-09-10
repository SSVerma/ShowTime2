package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class ReleaseDatesPayload(
    @SerializedName("results")
    val results: List<CountryReleaseDates>?
)

class CountryReleaseDates(
    @SerializedName("iso_3166_1")
    val countryCode: String?,

    @SerializedName("release_dates")
    val releaseDates: List<RemoteReleaseDate>?
)

class RemoteReleaseDate(
    @SerializedName("type")
    val type: Int,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("note")
    val note: String?
)
