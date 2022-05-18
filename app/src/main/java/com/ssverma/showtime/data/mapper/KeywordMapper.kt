package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteKeyword
import com.ssverma.core.domain.model.Keyword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteKeyword.asKeyword(): Keyword {
    return Keyword(
        id = id,
        name = name.orEmpty()
    )
}

suspend fun List<RemoteKeyword>.asKeywords() = withContext(Dispatchers.Default) {
    map { it.asKeyword() }
}