package com.ssverma.shared.domain.model

data class WatchProviderRegion(
    val iso31661: String, // e.g. "US", "IN"
    val englishName: String,
    val nativeName: String
)
