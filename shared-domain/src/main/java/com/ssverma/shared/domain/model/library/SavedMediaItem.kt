package com.ssverma.shared.domain.model.library

import com.ssverma.shared.domain.model.MediaType

data class SavedMediaItem(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String,
    val voteAvg: Float,
    val releaseDate: String,
    val addedAt: Long
)
