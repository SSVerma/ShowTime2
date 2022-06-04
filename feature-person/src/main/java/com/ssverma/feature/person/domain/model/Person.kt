package com.ssverma.feature.person.domain.model

import com.ssverma.core.domain.model.Gender
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.domain.model.MediaType
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