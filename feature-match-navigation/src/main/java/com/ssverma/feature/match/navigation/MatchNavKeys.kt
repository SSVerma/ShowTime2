package com.ssverma.feature.match.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class MatchRoomNavKey(
    val roomCode: String? = null,
    val initialMode: String? = null
) : NavKey, Parcelable
