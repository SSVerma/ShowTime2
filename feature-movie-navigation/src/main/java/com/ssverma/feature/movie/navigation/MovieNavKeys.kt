package com.ssverma.feature.movie.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object MovieHomeNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data class MovieDetailNavKey(val movieId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class MovieReviewsNavKey(val movieId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class MovieImageShotsNavKey(val movieId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class MovieImagePagerNavKey(val movieId: Int, val initialPageIndex: Int) : NavKey, Parcelable
