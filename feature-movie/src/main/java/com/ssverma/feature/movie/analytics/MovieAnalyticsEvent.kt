package com.ssverma.feature.movie.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.showtime.shared.analytics.SharedAnalyticsKeys

sealed class MovieAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap(),
) : AnalyticsEvent {

    data class MovieClicked(
        val movieId: Int,
        val movieTitle: String,
        val section: String,
        val sourceScreen: String,
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.MOVIE_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.MOVIE_ID to movieId,
            MovieAnalyticsKeys.MOVIE_TITLE to movieTitle,
            MovieAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    ) {
        constructor(
            movie: MoviePreview,
            section: String,
            sourceScreen: String
        ) : this(
            movieId = movie.id,
            movieTitle = movie.title,
            section = section,
            sourceScreen = sourceScreen
        )
    }

    data class GenreClicked(val genre: Genre, val sourceScreen: String) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.GENRE_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.GENRE_ID to genre.id,
            MovieAnalyticsKeys.GENRE_NAME to genre.name,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class SeeAllClicked(val section: String, val sourceScreen: String) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.SEE_ALL_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class WatchProviderClicked(
        val providerInfo: ProviderInfo,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.WATCH_PROVIDER_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.WATCH_PROVIDER_ID to providerInfo.providerId,
            MovieAnalyticsKeys.WATCH_PROVIDER_NAME to providerInfo.providerName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class CastClicked(
        val cast: Cast,
        val sourceScreen: String,
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.CAST_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.CAST_ID to cast.id,
            MovieAnalyticsKeys.CAST_NAME to cast.name,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class FilterClicked(val listingType: String) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.FILTER_CLICKED,
        params = mapOf(MovieAnalyticsKeys.LISTING_TYPE to listingType)
    )

    data class ImageShotClicked(
        val index: Int,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.IMAGE_SHOT_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.IMAGE_INDEX to index,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class JustWatchClicked(val sourceScreen: String) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.JUST_WATCH_CLICKED,
        params = mapOf(SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen)
    )

    data class VideoClicked(
        val video: Video,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.VIDEO_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.VIDEO_ID to video.id,
            MovieAnalyticsKeys.VIDEO_TITLE to video.type,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ReviewClicked(
        val review: Review,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.REVIEW_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.REVIEW_ID to review.id,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class KeywordClicked(
        val keyword: Keyword,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.KEYWORD_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.KEYWORD_ID to keyword.id,
            MovieAnalyticsKeys.KEYWORD_NAME to keyword.name,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class TrailerClicked(
        val movieId: Int,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.TRAILER_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.MOVIE_ID to movieId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class AddToStatsClicked(
        val movieId: Int,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.ADD_TO_STATS_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.MOVIE_ID to movieId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ShareClicked(
        val movieId: Int,
        val sourceScreen: String
    ) : MovieAnalyticsEvent(
        eventName = MovieAnalyticsEventName.SHARE_CLICKED,
        params = mapOf(
            MovieAnalyticsKeys.MOVIE_ID to movieId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
