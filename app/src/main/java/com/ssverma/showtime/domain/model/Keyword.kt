package com.ssverma.showtime.domain.model

import com.ssverma.showtime.data.remote.response.RemoteKeyword
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Keyword(
    val id: Int,
    val name: String
)

fun RemoteKeyword.asKeyword(): Keyword {
    return Keyword(
        id = id,
        name = name ?: ""
    )
}

suspend fun List<RemoteKeyword>.asKeywords() = withContext(Dispatchers.Default) {
    map { it.asKeyword() }
}