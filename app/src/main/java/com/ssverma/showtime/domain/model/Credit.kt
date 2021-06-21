package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteCast
import com.ssverma.showtime.data.remote.response.RemoteCrew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val avatarImageUrl: String,
    val creditId: String
)

data class Crew(
    val id: Int,
    val name: String,
    val avatarImageUrl: String,
    val creditId: String,
    val department: String,
    val job: String
)

fun RemoteCast.asCast(): Cast {
    return Cast(
        id = id,
        name = name ?: "",
        character = character ?: "",
        avatarImageUrl = profilePath.convertToFullTmdbImageUrl(),
        creditId = creditId ?: ""
    )
}

suspend fun List<RemoteCast>.asCasts() = withContext(Dispatchers.Default) {
    map { it.asCast() }
}

fun RemoteCrew.asCrew(): Crew {
    return Crew(
        id = id,
        name = name ?: "",
        avatarImageUrl = profilePath.convertToFullTmdbImageUrl(),
        creditId = creditId ?: "",
        department = department ?: "",
        job = job ?: ""
    )
}

suspend fun List<RemoteCrew>.asCrews() = withContext(Dispatchers.Default) {
    map { it.asCrew() }
}