package com.ssverma.showtime.domain.model

import androidx.annotation.StringRes
import com.ssverma.showtime.R
import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemotePerson
import com.ssverma.showtime.data.remote.response.RemotePersonMedia
import com.ssverma.showtime.domain.mapper.GenderMapper
import com.ssverma.showtime.domain.mapper.TmdbMediaTypeMapper
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.FormatterUtils
import com.ssverma.showtime.utils.formatLocally
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

sealed class MediaType(
    @StringRes open val displayNameRes: Int
) {
    object Movie : MediaType(R.string.movie)

    object Tv : MediaType(R.string.tv)

    object Unknown : MediaType(R.string.unknown)
}

sealed class Gender(
    @StringRes open val displayGenderRes: Int
) {
    data class Male(
        @StringRes override val displayGenderRes: Int
    ) : Gender(displayGenderRes)

    data class Female(
        @StringRes override val displayGenderRes: Int
    ) : Gender(displayGenderRes)

    object Unknown : Gender(R.string.unknown)
}


suspend fun RemotePersonMedia.asPersonMedia() = PersonMedia(
    id = id,
    title = title.emptyIfNull(),
    posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
    backdropImageUrl = backdropPath.convertToFullTmdbImageUrl(),
    character = character.emptyIfNull(),
    overview = overview.emptyIfNull(),
    displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
    releaseDate = DateUtils.parseIsoDate(releaseDate),
    popularity = popularity,
    displayPopularity = FormatterUtils.toRangeSymbol(popularity),
    genres = genres?.asGenres() ?: emptyList(),
    creditId = creditId,
    department = department,
    job = job,
    mediaType = TmdbMediaTypeMapper.map(mediaType ?: "")
)

suspend fun List<RemotePersonMedia>.asPersonMedias() = map { it.asPersonMedia() }

suspend fun RemotePerson.asPerson() = Person(
    id = id,
    name = name.emptyIfNull(),
    biography = biography.emptyIfNull(),
    imageUrl = profilePath.convertToFullTmdbImageUrl(),
    dob = DateUtils.parseIsoDate(dob)?.formatLocally(),
    knownFor = knownFor ?: "",
    gender = GenderMapper.map(gender),
    placeOfBirth = placeOfBirth ?: "",
    imageShots = personImage?.profileImages?.asImagesShots() ?: emptyList(),
    mediaByType = ((credit?.casts?.asPersonMedias() ?: emptyList())
            + (credit?.crews?.asPersonMedias() ?: emptyList()))
        .sortedByDescending { it.releaseDate }
        .distinctBy { it.title }
        .groupBy { it.mediaType },
    popularMedia = popularMedia?.asPersonMedias()
)

suspend fun List<RemotePerson>.asPersons() = map { it.asPerson() }