package com.ssverma.showtime.domain.repository

import androidx.paging.PagingData
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.TvDiscoverConfig
import com.ssverma.showtime.domain.failure.tv.TvEpisodeFailure
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.core.domain.model.Genre
import com.ssverma.core.domain.model.Review
import com.ssverma.showtime.domain.model.tv.*
import kotlinx.coroutines.flow.Flow

interface TvShowRepository {
    /**
     * Allows to find tv shows based on various query params in paginated form.
     */
    fun discoverTvShowsGradually(
        discoverConfig: TvDiscoverConfig
    ): Flow<PagingData<TvShow>>

    /**
     * Fetch trending tv shows based on the given [TimeWindow] and in paginated form.
     */
    fun fetchTrendingTvShowsGradually(
        timeWindow: TimeWindow
    ): Flow<PagingData<TvShow>>

    /**
     * Fetch tv shows in order of given rating and in paginated form.
     */
    fun fetchTopRatedTvShowsGradually(): Flow<PagingData<TvShow>>

    /**
     * Fetch top rated tv shows. It provides a limited collection of the top rated tv shows.
     * wheres [fetchTopRatedTvShowsGradually] can be used to fetch all available top rated tv shows
     * in paginated form.
     */
    suspend fun fetchTopRatedTvShows(): Result<List<TvShow>, Failure<TvShowFailure>>

    /**
     * Fetch first collection of trending tv shows based on the given [TimeWindow].
     */
    suspend fun fetchTrendingTvShows(
        timeWindow: TimeWindow
    ): Result<List<TvShow>, Failure<TvShowFailure>>

    /**
     * Allows to find tv shows based on various query params.
     */
    suspend fun discoverTvShows(
        discoverConfig: TvDiscoverConfig
    ): Result<List<TvShow>, Failure<TvShowFailure>>

    /**
     * Fetch all tv shows genres.
     */
    suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure>

    /**
     * Fetch all the tv show reviews by the given [tvShowId]
     */
    fun fetchTvShowReviewsGradually(
        tvShowId: Int
    ): Flow<PagingData<Review>>

    /**
     * Fetch details of a particular tv show.
     */
    suspend fun fetchTvShowDetails(
        detailsConfig: TvShowDetailsConfig
    ): Result<TvShow, Failure<TvShowFailure>>

    /**
     * Fetch details of a particular tv season.
     */
    suspend fun fetchTvSeasonDetails(
        seasonConfig: TvSeasonConfig
    ): Result<TvSeason, Failure<TvSeasonFailure>>

    /**
     * Fetch details of a particular tv episode.
     */
    suspend fun fetchTvEpisodeDetails(
        tvEpisodeConfig: TvEpisodeConfig
    ): Result<TvEpisode, Failure<TvEpisodeFailure>>
}