package com.ssverma.shared.domain.model.community

import com.ssverma.shared.domain.model.MediaType

data class TrendingDiscussion(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val backdropImageUrl: String?,
    val posterImageUrl: String?,
    val discussionCount: Int,
    val latestCommentSnippet: String? = null,
    val topTag: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
)
