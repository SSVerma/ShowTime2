package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteCast
import com.ssverma.api.service.tmdb.response.RemoteCrew
import com.ssverma.core.domain.model.Cast
import com.ssverma.core.domain.model.Crew
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun RemoteCast.asCast(): Cast {
    return Cast(
        id = id,
        name = name.orEmpty(),
        character = character.orEmpty(),
        avatarImageUrl = profilePath.convertToFullTmdbImageUrl(),
        creditId = creditId.orEmpty()
    )
}

suspend fun List<RemoteCast>.asCasts() = withContext(Dispatchers.Default) {
    map { it.asCast() }
}

fun RemoteCrew.asCrew(): Crew {
    return Crew(
        id = id,
        name = name.orEmpty(),
        avatarImageUrl = profilePath.convertToFullTmdbImageUrl(),
        creditId = creditId.orEmpty(),
        department = department.orEmpty(),
        job = job.orEmpty()
    )
}

suspend fun List<RemoteCrew>.asCrews() = withContext(Dispatchers.Default) {
    map { it.asCrew() }
}