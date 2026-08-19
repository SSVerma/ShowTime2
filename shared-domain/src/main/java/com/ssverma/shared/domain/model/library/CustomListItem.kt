package com.ssverma.shared.domain.model.library

import com.ssverma.shared.domain.model.MediaType

data class CustomListItem(
    val listId: String,
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val voteAvg: Float = 0f,
    val userNotes: String? = null,
    val rankOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
