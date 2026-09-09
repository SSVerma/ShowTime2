package com.ssverma.feature.community.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import com.ssverma.shared.domain.model.community.DiscussionTarget
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CommunityDiscussionsNavKey(
    val mediaType: String,
    val mediaId: Int,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val mediaTitle: String? = null,
    val posterImageUrl: String? = null,
    val backdropImageUrl: String? = null
) : NavKey, Parcelable {

    fun toDiscussionTarget(): DiscussionTarget =
        if (seasonNumber != null && episodeNumber != null) {
            DiscussionTarget.tvEpisode(mediaId, seasonNumber, episodeNumber)
        } else if (mediaType.equals("tv", ignoreCase = true)) {
            DiscussionTarget.tvShow(mediaId)
        } else {
            DiscussionTarget.movie(mediaId)
        }

    companion object {
        fun movie(
            movieId: Int,
            movieTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null
        ) = CommunityDiscussionsNavKey(
            mediaType = "movie",
            mediaId = movieId,
            mediaTitle = movieTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl
        )

        fun tvShow(
            tvShowId: Int,
            tvShowTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null
        ) = CommunityDiscussionsNavKey(
            mediaType = "tv",
            mediaId = tvShowId,
            mediaTitle = tvShowTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl
        )

        fun tvEpisode(
            tvShowId: Int,
            seasonNumber: Int,
            episodeNumber: Int,
            episodeTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null
        ) = CommunityDiscussionsNavKey(
            mediaType = "tv",
            mediaId = tvShowId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            mediaTitle = episodeTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl
        )
    }
}
