package com.ssverma.shared.domain.model

private const val YoutubeVideoUrl = "https://www.youtube.com/watch?v="
private const val YoutubeThumbnailUrl = "https://img.youtube.com/vi/"

data class Video(
    val id: String,
    val key: String,
    val name: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false,
    val publishedAt: String? = null,
    val iso6391: String? = null
)

fun Video.youtubeThumbnailUrl(): String {
    return "$YoutubeThumbnailUrl$key/mqdefault.jpg"
}

/**
 * Finds the most suitable trailer video from the list:
 * 1. Official Trailer (type = "Trailer", official = true)
 * 2. Any Trailer (type = "Trailer")
 * 3. Official Teaser (type = "Teaser", official = true)
 * 4. Any Teaser (type = "Teaser")
 * 5. First available video
 */
fun List<Video>.primaryTrailer(): Video? {
    return firstOrNull { it.official && it.type.equals("Trailer", ignoreCase = true) }
        ?: firstOrNull { it.type.equals("Trailer", ignoreCase = true) }
        ?: firstOrNull { it.official && it.type.equals("Teaser", ignoreCase = true) }
        ?: firstOrNull { it.type.equals("Teaser", ignoreCase = true) }
        ?: firstOrNull()
}

fun List<Video>.hasTrailer(): Boolean {
    return primaryTrailer() != null
}
