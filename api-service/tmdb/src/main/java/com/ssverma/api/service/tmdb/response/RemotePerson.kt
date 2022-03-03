package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemotePerson(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("biography")
    val biography: String?,

    @SerializedName("profile_path")
    val profilePath: String?,

    @SerializedName("birthday")
    val dob: String?,

    @SerializedName("known_for_department")
    val knownFor: String?,

    @SerializedName("gender")
    val gender: Int,

    @SerializedName("place_of_birth")
    val placeOfBirth: String?,

    @SerializedName("images")
    val personImage: RemotePersonImage?,

    @SerializedName("combined_credits")
    val credit: RemotePersonCredit?,

    @SerializedName("known_for")
    val popularMedia: List<RemotePersonMedia>?
)

class RemotePersonImage(
    @SerializedName("profiles")
    val profileImages: List<RemoteImageShot>?
)

class RemotePersonCredit(
    @SerializedName("cast")
    val casts: List<RemotePersonMedia>?,

    @SerializedName("crew")
    val crews: List<RemotePersonMedia>?
)

class RemotePersonMedia(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title", alternate = ["name"])
    val title: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("character")
    val character: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("release_date", alternate = ["first_air_date"])
    val releaseDate: String?,

    @SerializedName("popularity")
    val popularity: Float,

    @SerializedName("generes")
    val genres: List<RemoteGenre>?,

    @SerializedName("credit_id")
    val creditId: String?,

    @SerializedName("department")
    val department: String?,

    @SerializedName("job")
    val job: String?,

    @SerializedName("media_type")
    val mediaType: String?
)