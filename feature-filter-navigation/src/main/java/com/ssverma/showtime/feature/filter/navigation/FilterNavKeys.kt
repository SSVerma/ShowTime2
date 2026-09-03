package com.ssverma.showtime.feature.filter.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class WatchProviderHubNavKey(
    val providerId: Int,
    val providerName: String,
    val logoPath: String,
    val isMovie: Boolean,
    val source: String = "default"
) : NavKey, Parcelable

@Serializable
@Parcelize
data class UniversalDiscoveryNavKey(
    val initialMediaType: String = "Movie",
    val initialVibe: String = "ALL",
    val initialStudioHub: String? = null,
    val initialGenreId: Int? = null,
    val initialProviderId: Int? = null,
    val initialDecade: String? = null,
    val initialSortOrder: String? = null,
    val autoSpinRoulette: Boolean = false
) : NavKey, Parcelable

