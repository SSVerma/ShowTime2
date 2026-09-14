package com.ssverma.feature.tv.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.SequentialNavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data object TvShowHomeNavKey : NavKey, Parcelable

@Serializable
@Parcelize
data class TvShowDetailNavKey(val tvShowId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class TvShowReviewsNavKey(val tvShowId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class TvSeasonDetailNavKey(
    val tvShowId: Int,
    val seasonNumber: Int,
    val tvShowTitle: String? = null,
    val tvShowPosterPath: String? = null,
    val tvShowBackdropPath: String? = null
) : NavKey, Parcelable

@Serializable
@Parcelize
data class TvEpisodeDetailNavKey(
    val tvShowId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val tvShowTitle: String? = null,
    val tvShowPosterPath: String? = null,
    val tvShowBackdropPath: String? = null
) : SequentialNavKey, Parcelable {
    override val sequenceGroupId: Any
        get() = "$tvShowId-$seasonNumber"

    override val sequenceOrder: Int
        get() = episodeNumber
}

@Serializable
@Parcelize
data class TvShowImageShotsNavKey(val tvShowId: Int) : NavKey, Parcelable

@Serializable
@Parcelize
data class TvShowImagePagerNavKey(val tvShowId: Int, val initialPageIndex: Int) : NavKey, Parcelable
