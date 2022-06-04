package com.ssverma.shared.domain.model

data class ImageShot(
    val aspectRatio: Float,
    val imageUrl: String,
    val height: Int,
    val width: Int,
    val iso6391: String?
)

fun emptyImageShot() = ImageShot(
    aspectRatio = 0f,
    imageUrl = "",
    height = 0,
    width = 0,
    iso6391 = null
)