package com.ssverma.showtime.feature.filter.navigation

data class WatchProviderNavArgs(
    val providerId: Int,
    val providerName: String,
    val logoPath: String,
    val isMovie: Boolean
)
