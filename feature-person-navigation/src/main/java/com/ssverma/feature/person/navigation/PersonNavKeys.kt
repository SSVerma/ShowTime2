package com.ssverma.feature.person.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.AdaptiveDetailNavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object PersonHomeNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data class PersonDetailNavKey(
    val personId: Int,
    val personName: String? = null,
    val personImageUrl: String? = null,
    val source: String = "default"
) : AdaptiveDetailNavKey, Parcelable

@Serializable
@Parcelize
data class PersonImageShotsNavKey(val personId: Int) : NavKey, Parcelable
