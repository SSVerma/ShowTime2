package com.ssverma.shared.domain.model.person

import com.ssverma.shared.domain.model.Gender
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.MediaType
import java.time.LocalDate

data class Person(
    val id: Int,
    val name: String,
    val biography: String,
    val imageUrl: String,
    val knownFor: String,
    val dob: String?,
    val deathday: String? = null,
    val gender: Gender,
    val placeOfBirth: String,
    val alsoKnownAs: List<String> = emptyList(),
    val homepage: String? = null,
    val imdbId: String? = null,
    val externalIds: PersonExternalIds? = null,
    val imageShots: List<ImageShot>,
    val mediaByType: Map<MediaType, List<PersonMedia>>,
    val popularMedia: List<PersonMedia>?,
    val popularity: Float,
    val displayPopularity: String,
    val age: Int? = null,
    val isDeceased: Boolean = false
)

data class PersonExternalIds(
    val imdbId: String? = null,
    val facebookId: String? = null,
    val instagramId: String? = null,
    val twitterId: String? = null,
    val tiktokId: String? = null,
    val wikidataId: String? = null,
    val youtubeId: String? = null
) {
    val hasSocialLinks: Boolean
        get() = !imdbId.isNullOrBlank() ||
                !instagramId.isNullOrBlank() ||
                !twitterId.isNullOrBlank() ||
                !tiktokId.isNullOrBlank() ||
                !youtubeId.isNullOrBlank() ||
                !facebookId.isNullOrBlank() ||
                !wikidataId.isNullOrBlank()
}

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
    val voteAverage: Float,
    val voteCount: Int,
    val genres: List<Genre>,
    val creditId: String?,
    val department: String?,
    val job: String?,
    val mediaType: MediaType
)