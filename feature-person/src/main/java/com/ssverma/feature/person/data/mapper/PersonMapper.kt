package com.ssverma.feature.person.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.api.service.tmdb.response.RemotePersonCredit
import com.ssverma.api.service.tmdb.response.RemotePersonMedia
import com.ssverma.core.domain.model.MediaType
import com.ssverma.core.domain.utils.DateUtils
import com.ssverma.core.domain.utils.FormatterUtils
import com.ssverma.core.domain.utils.formatLocally
import com.ssverma.feature.person.domain.model.Person
import com.ssverma.feature.person.domain.model.PersonMedia
import com.ssverma.shared.data.mapper.*
import javax.inject.Inject

class PersonMapper @Inject constructor(
    private val genderMapper: GenderMapper,
    private val mediaTypeMapper: TmdbMediaTypeMapper
) : Mapper<RemotePerson, Person>() {
    override suspend fun map(input: RemotePerson): Person {
        return input.asPerson(
            genderMapper = genderMapper,
            mediaTypeMapper = mediaTypeMapper
        )
    }
}

class PersonsMapper @Inject constructor(
    private val genderMapper: GenderMapper,
    private val mediaTypeMapper: TmdbMediaTypeMapper
) : ListMapper<RemotePerson, Person>() {
    override suspend fun mapItem(input: RemotePerson): Person {
        return input.asPerson(
            genderMapper = genderMapper,
            mediaTypeMapper = mediaTypeMapper
        )
    }
}

private suspend fun RemotePerson.asPerson(
    genderMapper: GenderMapper,
    mediaTypeMapper: TmdbMediaTypeMapper
) = Person(
    id = id,
    name = name.orEmpty(),
    biography = biography.orEmpty(),
    imageUrl = profilePath.convertToFullTmdbImageUrl(),
    dob = DateUtils.parseIsoDate(dob)?.formatLocally(),
    knownFor = knownFor.orEmpty(),
    gender = genderMapper.map(gender),
    placeOfBirth = placeOfBirth.orEmpty(),
    imageShots = personImage?.profileImages?.asImagesShots().orEmpty(),
    mediaByType = credit?.asMediaByType(mediaTypeMapper).orEmpty(),
    popularMedia = popularMedia?.asPersonMedias(mediaTypeMapper)
)

private suspend fun RemotePersonCredit.asMediaByType(
    mediaTypeMapper: TmdbMediaTypeMapper
): Map<MediaType, List<PersonMedia>> {
    val castWithCrews = ((this.casts?.asPersonMedias(mediaTypeMapper).orEmpty())
            + (this.crews?.asPersonMedias(mediaTypeMapper).orEmpty()))

    return castWithCrews
        .sortedByDescending { it.releaseDate }
        .distinctBy { it.title }
        .groupBy { it.mediaType }
}

private suspend fun RemotePersonMedia.asPersonMedia(
    mediaTypeMapper: TmdbMediaTypeMapper
) = PersonMedia(
    id = id,
    title = title.orEmpty(),
    posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
    backdropImageUrl = backdropPath.convertToFullTmdbImageUrl(),
    character = character.orEmpty(),
    overview = overview.orEmpty(),
    displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
    releaseDate = DateUtils.parseIsoDate(releaseDate),
    popularity = popularity,
    displayPopularity = FormatterUtils.toRangeSymbol(popularity),
    genres = genres?.asGenres() ?: emptyList(),
    creditId = creditId,
    department = department,
    job = job,
    mediaType = mediaTypeMapper.map(mediaType.orEmpty())
)

private suspend fun List<RemotePersonMedia>.asPersonMedias(
    mediaTypeMapper: TmdbMediaTypeMapper
) = map { it.asPersonMedia(mediaTypeMapper) }

