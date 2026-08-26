package com.ssverma.feature.account.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object ProfileNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object BackupSyncNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data object TraktSyncNavKey : NavKey, Parcelable
