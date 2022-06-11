package com.ssverma.api.service.tmdb


object TmdbDefaults {
    object ApiDefaults {
        const val FirstPageNumber = 1
        const val PageSize = 20
    }

    object ImageDefaults {
        const val ImageBaseUrl = "https://image.tmdb.org/t/p/w500" //TODO: Fetch from configs
    }
}

fun String?.convertToFullTmdbImageUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.ImageBaseUrl, this)
}

fun buildImageUrl(baseUrl: String, imagePath: String?): String {
    return imagePath?.let { path ->
        "$baseUrl/$path"
    }.orEmpty()
}