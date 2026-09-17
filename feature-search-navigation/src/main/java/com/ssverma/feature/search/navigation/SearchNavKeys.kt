package com.ssverma.feature.search.navigation

import android.os.Parcelable
import com.ssverma.core.navigation.nav3.TopRevealNavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object SearchNavKey : TopRevealNavKey, Parcelable
