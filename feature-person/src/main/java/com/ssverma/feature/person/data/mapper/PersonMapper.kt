package com.ssverma.feature.person.data.mapper

import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.api.service.tmdb.convertToTmdbProfileUrl
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.api.service.tmdb.response.RemotePersonCredit
import com.ssverma.api.service.tmdb.response.RemotePersonMedia
import com.ssverma.shared.data.mapper.GenderMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.data.mapper.TmdbMediaTypeMapper
import com.ssverma.shared.data.mapper.asGenres
import com.ssverma.shared.data.mapper.asImagesShots
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonExternalIds
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.domain.utils.formatLocally
import java.time.LocalDate
import java.time.Period
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
): Person {
    val parsedDob = DateUtils.parseIsoDate(dob)
    val parsedDeathday = DateUtils.parseIsoDate(deathday)
    val isDeceased = parsedDeathday != null

    val calculatedAge = when {
        parsedDob != null && parsedDeathday != null -> {
            Period.between(parsedDob, parsedDeathday).years.coerceAtLeast(0)
        }

        parsedDob != null -> {
            Period.between(parsedDob, LocalDate.now()).years.coerceAtLeast(0)
        }

        else -> null
    }

    val mappedExternalIds = externalIds?.let {
        PersonExternalIds(
            imdbId = it.imdbId?.takeIf { s -> s.isNotBlank() }
                ?: imdbId?.takeIf { s -> s.isNotBlank() },
            facebookId = it.facebookId?.takeIf { s -> s.isNotBlank() },
            instagramId = it.instagramId?.takeIf { s -> s.isNotBlank() },
            twitterId = it.twitterId?.takeIf { s -> s.isNotBlank() },
            tiktokId = it.tiktokId?.takeIf { s -> s.isNotBlank() },
            wikidataId = it.wikidataId?.takeIf { s -> s.isNotBlank() },
            youtubeId = it.youtubeId?.takeIf { s -> s.isNotBlank() }
        )
    } ?: imdbId?.takeIf { it.isNotBlank() }?.let {
        PersonExternalIds(imdbId = it)
    }

    return Person(
        id = id,
        name = name.orEmpty(),
        biography = biography.orEmpty(),
        imageUrl = profilePath.convertToTmdbProfileUrl(),
        dob = parsedDob?.formatLocally(),
        deathday = parsedDeathday?.formatLocally(),
        knownFor = knownFor.orEmpty(),
        gender = genderMapper.map(gender),
        placeOfBirth = placeOfBirth.orEmpty(),
        alsoKnownAs = alsoKnownAs.orEmpty().filter { it.isNotBlank() },
        homepage = homepage?.takeIf { it.isNotBlank() },
        imdbId = imdbId?.takeIf { it.isNotBlank() }
            ?: externalIds?.imdbId?.takeIf { it.isNotBlank() },
        externalIds = mappedExternalIds,
        imageShots = personImage?.profileImages?.asImagesShots().orEmpty(),
        mediaByType = credit?.asMediaByType(mediaTypeMapper).orEmpty(),
        popularMedia = popularMedia?.asPersonMedias(mediaTypeMapper),
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        age = calculatedAge,
        isDeceased = isDeceased
    )
}

private suspend fun RemotePersonCredit.asMediaByType(
    mediaTypeMapper: TmdbMediaTypeMapper
): Map<MediaType, List<PersonMedia>> {
    val castWithCrews = ((this.casts?.asPersonMedias(mediaTypeMapper).orEmpty())
            + (this.crews?.asPersonMedias(mediaTypeMapper).orEmpty()))

    return castWithCrews
        .sortedByDescending { it.releaseDate }
        .distinctBy { it.id to it.mediaType }
        .groupBy { it.mediaType }
}

private suspend fun RemotePersonMedia.asPersonMedia(
    mediaTypeMapper: TmdbMediaTypeMapper
) = PersonMedia(
    id = id,
    title = title.orEmpty(),
    posterImageUrl = posterPath.convertToTmdbPosterUrl(),
    backdropImageUrl = backdropPath.convertToTmdbBackdropUrl(),
    character = character.orEmpty(),
    overview = overview.orEmpty(),
    displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
    releaseDate = DateUtils.parseIsoDate(releaseDate),
    popularity = popularity,
    displayPopularity = FormatterUtils.toRangeSymbol(popularity),
    voteAverage = voteAverage,
    voteCount = voteCount,
    genres = genres?.asGenres() ?: emptyList(),
    creditId = creditId,
    department = department,
    job = job,
    mediaType = mediaTypeMapper.map(mediaType.orEmpty())
)

private suspend fun List<RemotePersonMedia>.asPersonMedias(
    mediaTypeMapper: TmdbMediaTypeMapper
) = map { it.asPersonMedia(mediaTypeMapper) }
