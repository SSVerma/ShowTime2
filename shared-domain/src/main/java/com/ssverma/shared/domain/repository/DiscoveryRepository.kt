package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow

interface DiscoveryRepository {
    suspend fun discoverMovies(
        discoverConfig: MovieDiscoverConfig
    ): Result<List<Movie>, Failure.CoreFailure>

    suspend fun discoverTvShows(
        discoverConfig: TvDiscoverConfig
    ): Result<List<TvShow>, Failure.CoreFailure>

    suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure>

    suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure>
}
