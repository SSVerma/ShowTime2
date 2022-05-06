package com.ssverma.showtime.domain.model

import java.time.LocalDate

data class Person(
    val id: Int,
    val name: String,
    val biography: String,
    val imageUrl: String,
    val knownFor: String,
    val dob: String?,
    val gender: Gender,
    val placeOfBirth: String,
    val imageShots: List<ImageShot>,
    val mediaByType: Map<MediaType, List<PersonMedia>>,
    val popularMedia: List<PersonMedia>?
)

data class PersonMedia(
    val id: Int,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val character: String,
    val overview: String,
    val displayReleaseDate: String?,
    val releaseDate: LocalDate?,
    val popularity: Float,
    val displayPopularity: String,
    val genres: List<Genre>,
    val creditId: String?,
    val department: String?,
    val job: String?,
    val mediaType: MediaType
)

sealed interface MediaType {
    object Movie : MediaType
    object Tv : MediaType
    object Unknown : MediaType
}

sealed interface Gender {
    object Male : Gender
    object Female : Gender
    object Unknown : Gender
}