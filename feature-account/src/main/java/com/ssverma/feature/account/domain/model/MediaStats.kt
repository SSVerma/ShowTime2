package com.ssverma.feature.account.domain.model

data class MediaStats(
    val mediaId: Int,
    val favorite: Boolean,
    val inWatchlist: Boolean,
    val isWatched: Boolean = false,
    val rating: Int?
)
