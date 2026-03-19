package com.ssverma.feature.tv.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.Video
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class TvAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap(),
) : AnalyticsEvent {

    data class TvShowClicked(
        val tvShowId: Int,
        val tvShowTitle: String,
        val section: String,
        val sourceScreen: String,
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.TV_SHOW_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            TvAnalyticsKeys.TV_SHOW_TITLE to tvShowTitle,
            TvAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    ) {
        constructor(
            tvShow: TvShowPreview,
            section: String,
            sourceScreen: String
        ) : this(
            tvShowId = tvShow.id,
            tvShowTitle = tvShow.title,
            section = section,
            sourceScreen = sourceScreen
        )
    }

    data class GenreClicked(
        val genreId: Int,
        val genreName: String,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.GENRE_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.GENRE_ID to genreId,
            TvAnalyticsKeys.GENRE_NAME to genreName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    ) {
        constructor(genre: Genre, sourceScreen: String) : this(
            genreId = genre.id,
            genreName = genre.name,
            sourceScreen = sourceScreen
        )
    }

    data class KeywordClicked(
        val keywordId: Int,
        val keywordName: String,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.KEYWORD_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.KEYWORD_ID to keywordId,
            TvAnalyticsKeys.KEYWORD_NAME to keywordName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    ) {
        constructor(keyword: Keyword, sourceScreen: String) : this(
            keywordId = keyword.id,
            keywordName = keyword.name,
            sourceScreen = sourceScreen
        )
    }

    data class SeeAllClicked(val section: String, val sourceScreen: String) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.SEE_ALL_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class WatchProviderClicked(
        val providerInfo: ProviderInfo,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.WATCH_PROVIDER_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.WATCH_PROVIDER_ID to providerInfo.providerId,
            TvAnalyticsKeys.WATCH_PROVIDER_NAME to providerInfo.providerName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class CastClicked(
        val cast: Cast,
        val sourceScreen: String,
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.CAST_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.CAST_ID to cast.id,
            TvAnalyticsKeys.CAST_NAME to cast.name,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class FilterClicked(val listingType: String) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.FILTER_CLICKED,
        params = mapOf(TvAnalyticsKeys.LISTING_TYPE to listingType)
    )

    data class SeasonClicked(
        val season: TvSeason,
        val tvShowId: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.SEASON_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            TvAnalyticsKeys.SEASON_ID to season.id,
            TvAnalyticsKeys.SEASON_NUMBER to season.seasonNumber,
            TvAnalyticsKeys.SEASON_NAME to season.title,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class EpisodeClicked(
        val episode: TvEpisode,
        val tvShowId: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.EPISODE_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            TvAnalyticsKeys.EPISODE_ID to episode.id,
            TvAnalyticsKeys.EPISODE_NUMBER to episode.episodeNumber,
            TvAnalyticsKeys.EPISODE_NAME to episode.title,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class ImageShotClicked(
        val index: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.IMAGE_SHOT_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.IMAGE_INDEX to index,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class JustWatchClicked(val sourceScreen: String) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.JUST_WATCH_CLICKED,
        params = mapOf(SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen)
    )

    data class VideoClicked(
        val video: Video,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.VIDEO_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.VIDEO_ID to video.id,
            TvAnalyticsKeys.VIDEO_TITLE to video.type,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ReviewClicked(
        val review: Review,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.REVIEW_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.REVIEW_ID to review.id,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class TrailerClicked(
        val tvShowId: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.TRAILER_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class AddToStatsClicked(
        val tvShowId: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.ADD_TO_STATS_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class ShareClicked(
        val tvShowId: Int,
        val sourceScreen: String
    ) : TvAnalyticsEvent(
        eventName = TvAnalyticsEventName.SHARE_CLICKED,
        params = mapOf(
            TvAnalyticsKeys.TV_SHOW_ID to tvShowId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
