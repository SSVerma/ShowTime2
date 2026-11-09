package com.ssverma.feature.community.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import com.ssverma.shared.domain.model.community.DiscussionTarget
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
sealed interface DiscussionNavTarget : Parcelable {
    val mediaId: Int

    fun toDiscussionTarget(): DiscussionTarget

    @Serializable
    @Parcelize
    data class Movie(val movieId: Int) : DiscussionNavTarget {
        override val mediaId: Int get() = movieId
        override fun toDiscussionTarget(): DiscussionTarget = DiscussionTarget.movie(movieId)
    }

    @Serializable
    @Parcelize
    data class TvShow(val tvShowId: Int) : DiscussionNavTarget {
        override val mediaId: Int get() = tvShowId
        override fun toDiscussionTarget(): DiscussionTarget = DiscussionTarget.tvShow(tvShowId)
    }

    @Serializable
    @Parcelize
    data class TvEpisode(
        val tvShowId: Int,
        val seasonNumber: Int,
        val episodeNumber: Int,
    ) : DiscussionNavTarget {
        override val mediaId: Int get() = tvShowId
        override fun toDiscussionTarget(): DiscussionTarget =
            DiscussionTarget.tvEpisode(tvShowId, seasonNumber, episodeNumber)
    }
}

@Serializable
@Parcelize
data class CommunityDiscussionsNavKey(
    val target: DiscussionNavTarget,
    val mediaTitle: String? = null,
    val posterImageUrl: String? = null,
    val backdropImageUrl: String? = null,
) : NavKey, Parcelable {

    val mediaId: Int get() = target.mediaId

    fun toDiscussionTarget(): DiscussionTarget = target.toDiscussionTarget()

    companion object {
        fun movie(
            movieId: Int,
            movieTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null,
        ) = CommunityDiscussionsNavKey(
            target = DiscussionNavTarget.Movie(movieId = movieId),
            mediaTitle = movieTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
        )

        fun tvShow(
            tvShowId: Int,
            tvShowTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null,
        ) = CommunityDiscussionsNavKey(
            target = DiscussionNavTarget.TvShow(tvShowId = tvShowId),
            mediaTitle = tvShowTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
        )

        fun tvEpisode(
            tvShowId: Int,
            seasonNumber: Int,
            episodeNumber: Int,
            episodeTitle: String? = null,
            posterImageUrl: String? = null,
            backdropImageUrl: String? = null,
        ) = CommunityDiscussionsNavKey(
            target = DiscussionNavTarget.TvEpisode(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
            ),
            mediaTitle = episodeTitle,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
        )
    }
}
