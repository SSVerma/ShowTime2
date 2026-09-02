package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.DiscoveryRepository

class FakeDiscoveryRepository : DiscoveryRepository {

    var mockUniversalItems: List<UniversalMediaItem> = listOf(
        UniversalMediaItem(
            id = 1,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            overview = "Paul Atreides unites with Chani and the Fremen.",
            posterImageUrl = "/dune2.jpg",
            backdropImageUrl = "/dune2_backdrop.jpg",
            voteAvg = 8.5f,
            voteCount = 4500,
            releaseDate = "2024-03-01"
        ),
        UniversalMediaItem(
            id = 2,
            mediaType = MediaType.Tv,
            title = "Severance",
            overview = "Mark leads a team of office workers whose memories have been surgically divided.",
            posterImageUrl = "/severance.jpg",
            backdropImageUrl = "/severance_backdrop.jpg",
            voteAvg = 8.7f,
            voteCount = 2100,
            releaseDate = "2022-02-18"
        )
    )

    override suspend fun discoverMovies(discoverConfig: MovieDiscoverConfig): Result<List<Movie>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }

    override suspend fun discoverTvShows(discoverConfig: TvDiscoverConfig): Result<List<TvShow>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }

    override suspend fun discoverUniversal(
        filter: UniversalDiscoveryFilter,
        page: Int
    ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
        val filtered = mockUniversalItems.filter { item ->
            if (filter.mediaType == MediaType.Movie) item.mediaType == MediaType.Movie
            else item.mediaType == MediaType.Tv
        }
        return Result.Success(filtered.ifEmpty { mockUniversalItems })
    }

    override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }

    override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }

    override suspend fun fetchWatchProviders(isMovie: Boolean): Result<List<ProviderInfo>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }
}
