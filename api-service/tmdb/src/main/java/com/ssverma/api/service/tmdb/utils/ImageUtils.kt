package com.ssverma.api.service.tmdb.utils

object ImageUtils {
    fun buildImageUrl(baseUrl: String, imagePath: String?): String {
        return imagePath?.let { path ->
            "$baseUrl/$path"
        } ?: ""
    }
}