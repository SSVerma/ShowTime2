package com.ssverma.api.service.tmdb

import androidx.paging.PagingConfig


object TmdbDefaults {
    object ApiDefaults {
        const val FirstPageNumber = 1
        const val PageSize = 20
    }

    object ImageDefaults {
        const val ImageBaseUrl = "https://image.tmdb.org/t/p/w500" //TODO: Fetch from configs
        const val PosterBaseUrl = "https://image.tmdb.org/t/p/w342"
        const val BackdropBaseUrl = "https://image.tmdb.org/t/p/w780"
        const val ProfileBaseUrl = "https://image.tmdb.org/t/p/w185"
        const val LogoBaseUrl = "https://image.tmdb.org/t/p/w154"
        const val OriginalBaseUrl = "https://image.tmdb.org/t/p/original"
    }

    fun authApprovalRedirectUrl(requestToken: String): String {
        return "https://www.themoviedb.org/auth/access?request_token=$requestToken"
    }

    fun pagingConfig(
        pageSize: Int = ApiDefaults.PageSize,
        prefetchDistance: Int = pageSize,
        enablePlaceholders: Boolean = false
    ): PagingConfig {
        return PagingConfig(
            pageSize = pageSize,
            prefetchDistance = prefetchDistance,
            enablePlaceholders = enablePlaceholders
        )
    }
}

fun String?.convertToFullTmdbImageUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.ImageBaseUrl, this)
}

fun String?.convertToTmdbPosterUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.PosterBaseUrl, this)
}

fun String?.convertToTmdbBackdropUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.BackdropBaseUrl, this)
}

fun String?.convertToTmdbProfileUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.ProfileBaseUrl, this)
}

fun String?.convertToTmdbLogoUrl(): String {
    return buildImageUrl(TmdbDefaults.ImageDefaults.LogoBaseUrl, this)
}

fun buildImageUrl(baseUrl: String, imagePath: String?): String {
    return imagePath?.let { path ->
        if (path.isBlank()) return@let ""
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        val normalizedBase = baseUrl.trimEnd('/')
        "$normalizedBase$normalizedPath"
    }.orEmpty()
}
