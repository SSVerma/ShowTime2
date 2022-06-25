package com.ssverma.feature.search.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.shared.data.mapper.GenderMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.domain.utils.formatLocally
import javax.inject.Inject

class SearchSuggestionsMapper @Inject constructor(
    private val genderMapper: GenderMapper
) : ListMapper<RemoteMultiSearchSuggestion, SearchSuggestion>() {
    override suspend fun mapItem(input: RemoteMultiSearchSuggestion): SearchSuggestion {
        return input.asSearchSuggestion(genderMapper)
    }
}

class SearchSuggestionMapper @Inject constructor(
    private val genderMapper: GenderMapper
) : Mapper<RemoteMultiSearchSuggestion, SearchSuggestion>() {

    override suspend fun map(input: RemoteMultiSearchSuggestion): SearchSuggestion {
        return input.asSearchSuggestion(genderMapper)
    }
}

private suspend fun RemoteMultiSearchSuggestion.asSearchSuggestion(
    genderMapper: GenderMapper
): SearchSuggestion {
    return when (mediaType) {
        TmdbApiTiedConstants.AvailableMediaTypes.Movie -> {
            asMovie()
        }
        TmdbApiTiedConstants.AvailableMediaTypes.Tv -> {
            asTvShow()
        }
        TmdbApiTiedConstants.AvailableMediaTypes.Person -> {
            asPerson(genderMapper)
        }
        else -> {
            SearchSuggestion.None
        }
    }
}

private fun RemoteMultiSearchSuggestion.asMovie(): SearchSuggestion.Movie {
    return SearchSuggestion.Movie(
        id = id,
        title = name.orEmpty(),
        overview = overview.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        backdropImageUrl = backdropPath.convertToFullTmdbImageUrl(),
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayReleaseDate = DateUtils.parseIsoDate(releaseDate)?.formatLocally(),
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage.orEmpty()
    )
}

private fun RemoteMultiSearchSuggestion.asTvShow(): SearchSuggestion.TvShow {
    return SearchSuggestion.TvShow(
        id = id,
        title = name.orEmpty(),
        overview = overview.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        backdropImageUrl = backdropPath.convertToFullTmdbImageUrl(),
        voteAvg = voteAvg,
        voteAvgPercentage = voteAvg * 10f,
        voteCount = voteCount,
        displayFirstAirDate = DateUtils.parseIsoDate(firstAirDate)?.formatLocally(),
        popularity = popularity,
        displayPopularity = FormatterUtils.toRangeSymbol(popularity),
        originalLanguage = originalLanguage.orEmpty()
    )
}

private suspend fun RemoteMultiSearchSuggestion.asPerson(
    genderMapper: GenderMapper
): SearchSuggestion.Person {
    return SearchSuggestion.Person(
        id = id,
        name = name.orEmpty(),
        imageUrl = profilePath.convertToFullTmdbImageUrl(),
        department = department.orEmpty(),
        popularity = popularity,
        gender = genderMapper.map(gender),
    )
}