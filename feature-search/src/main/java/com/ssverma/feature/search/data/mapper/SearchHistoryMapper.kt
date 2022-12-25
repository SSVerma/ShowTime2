package com.ssverma.feature.search.data.mapper

import com.ssverma.feature.search.data.local.db.LocalSearchHistory
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.domain.model.MediaType
import javax.inject.Inject

class HistoryToLocalHistoryMapper @Inject constructor() :
    Mapper<SearchHistory, LocalSearchHistory>() {
    override suspend fun map(input: SearchHistory): LocalSearchHistory {
        return LocalSearchHistory(
            id = input.id,
            name = input.name,
            mediaType = input.mediaType.asLocalMediaType(),
            timestamp = System.currentTimeMillis()
        )
    }
}

class LocalHistoryToHistoryListMapper @Inject constructor() :
    ListMapper<LocalSearchHistory, SearchHistory>() {

    override suspend fun mapItem(input: LocalSearchHistory): SearchHistory {
        return SearchHistory(
            id = input.id,
            name = input.name,
            mediaType = input.mediaType.asMediaType()
        )
    }
}

private fun Int.asMediaType(): MediaType {
    return when (this) {
        AvailableLocalMediaTypes.Movie -> MediaType.Movie
        AvailableLocalMediaTypes.TvShow -> MediaType.Tv
        AvailableLocalMediaTypes.Person -> MediaType.Person
        else -> MediaType.Unknown
    }
}

private fun MediaType.asLocalMediaType(): Int {
    return when (this) {
        MediaType.Movie -> AvailableLocalMediaTypes.Movie
        MediaType.Person -> AvailableLocalMediaTypes.Person
        MediaType.Tv -> AvailableLocalMediaTypes.TvShow
        MediaType.Unknown -> AvailableLocalMediaTypes.None
    }
}

private object AvailableLocalMediaTypes {
    const val Movie = 1
    const val TvShow = 2
    const val Person = 3
    const val None = -1
}