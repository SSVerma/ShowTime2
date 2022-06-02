package com.ssverma.core.domain.utils

object CoreUtils {
    fun buildImageUrl(baseUrl: String, imagePath: String?): String {
        return imagePath?.let { path ->
            "$baseUrl/$path"
        } ?: ""
    }
}