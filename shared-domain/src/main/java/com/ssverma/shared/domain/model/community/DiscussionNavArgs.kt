package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType

data class DiscussionNavArgs(
    val mediaType: MediaType,
    val mediaId: Int,
    val title: String,
    val posterImageUrl: String? = null,
    val backdropImageUrl: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
)
