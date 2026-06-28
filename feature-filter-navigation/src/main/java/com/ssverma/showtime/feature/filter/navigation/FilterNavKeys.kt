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
    val isMovie: Boolean
) : NavKey, Parcelable
