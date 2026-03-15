package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteLanguage
import com.ssverma.shared.domain.model.Language

fun RemoteLanguage.asLanguage(): Language {
    return Language(
        iso6391 = iso6391,
        englishName = englishName,
        name = name
    )
}

fun List<RemoteLanguage>.asLanguages(): List<Language> {
    return map { it.asLanguage() }
}
