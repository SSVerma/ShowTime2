package com.ssverma.feature.search.domain.model

import com.ssverma.shared.domain.model.MediaType

data class SearchHistory(
    val id: Int,
    val name: String,
    val mediaType: MediaType
)