package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType

data class CommunityCuratedListItem(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val voteAvg: Float = 0f,
    val rankOrder: Int = 0
)
