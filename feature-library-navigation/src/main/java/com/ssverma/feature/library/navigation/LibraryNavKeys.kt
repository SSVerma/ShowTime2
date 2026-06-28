package com.ssverma.feature.library.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object LibraryHomeNavKey : NavKey, Parcelable
