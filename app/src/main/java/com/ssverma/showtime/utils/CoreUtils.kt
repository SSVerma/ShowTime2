package com.ssverma.showtime.utils

object CoreUtils {
    fun buildImageUrl(baseUrl: String, imagePath: String?): String {
        return imagePath?.let { path ->
            "$baseUrl/$path"
        } ?: ""
    }
}