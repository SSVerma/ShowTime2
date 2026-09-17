package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.data.mapper.GenresMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryLanguage
import com.ssverma.shared.domain.model.discovery.DiscoveryMonetizationType
import com.ssverma.shared.domain.model.discovery.DiscoveryRatingThreshold
import com.ssverma.shared.domain.model.discovery.DiscoveryRuntimeRange
import com.ssverma.shared.domain.model.discovery.DiscoveryStudioHub
import com.ssverma.shared.domain.model.discovery.DiscoveryTvNetworkHub
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultDiscoveryRepositoryTest {

    private val mockTmdbApiService: TmdbApiService = mockk()
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockMoviesMapper: ListMapper<RemoteMovie, Movie> = mockk(relaxed = true)
    private val mockTvShowsMapper: ListMapper<RemoteTvShow, TvShow> = mockk(relaxed = true)
    private val mockGenresMapper: GenresMapper = mockk(relaxed = true)

    private lateinit var repository: DefaultDiscoveryRepository

    @Before
    fun setUp() {
        repository = DefaultDiscoveryRepository(
            tmdbApiService = mockTmdbApiService,
            appConfigRepository = mockAppConfigRepository,
            moviesMapper = mockMoviesMapper,
            tvShowsMapper = mockTvShowsMapper,
            genresMapper = mockGenresMapper
        )
    }

    @Test
    fun `discoverUniversal maps movie filters into TMDB query parameters correctly`() = runTest {
        val capturedQueryMap = slot<Map<String, String>>()
        val fakeMovie = mockk<RemoteMovie>(relaxed = true) {
            every { id } returns 100
            every { title } returns "Civil War"
            every { overview } returns "Dystopian war"
            every { posterPath } returns "/poster.jpg"
            every { backdropPath } returns "/backdrop.jpg"
            every { voteAvg } returns 7.5f
            every { voteCount } returns 2500
            every { releaseDate } returns "2024-04-12"
        }
        val pagedPayload = mockk<PagedPayload<RemoteMovie>>(relaxed = true) {
            every { results } returns listOf(fakeMovie)
        }

        coEvery {
            mockTmdbApiService.getDiscoveredMovies(capture(capturedQueryMap), any())
        } returns ApiResponse.Success(body = pagedPayload, payload = mockk(relaxed = true))

        val filter = UniversalDiscoveryFilter(
            mediaType = MediaType.Movie,
            studioHub = DiscoveryStudioHub.A24,
            ratingThreshold = DiscoveryRatingThreshold.GREAT_7_PLUS,
            runtimeRange = DiscoveryRuntimeRange.SHORT,
            monetizationTypes = setOf(
                DiscoveryMonetizationType.STREAM,
                DiscoveryMonetizationType.RENT
            ),
            language = DiscoveryLanguage.ENGLISH,
            certification = "R",
            watchRegion = "US"
        )

        val result = repository.discoverUniversal(filter, page = 1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val queryMap = capturedQueryMap.captured
        assertThat(queryMap["with_companies"]).isEqualTo("41077") // A24 TMDB ID
        assertThat(queryMap["vote_average.gte"]).isEqualTo("7.0")
        assertThat(queryMap["with_runtime.lte"]).isEqualTo("90")
        assertThat(queryMap["with_watch_monetization_types"]).contains("flatrate")
        assertThat(queryMap["with_watch_monetization_types"]).contains("rent")
        assertThat(queryMap["with_original_language"]).isEqualTo("en")
        assertThat(queryMap["certification_country"]).isEqualTo("US")
        assertThat(queryMap["certification"]).isEqualTo("R")
    }

    @Test
    fun `discoverUniversal maps TV filters and network hubs into TMDB query parameters correctly`() =
        runTest {
            val capturedQueryMap = slot<Map<String, String>>()
            val fakeTv = mockk<RemoteTvShow>(relaxed = true) {
                every { id } returns 200
                every { title } returns "Succession"
                every { overview } returns "Media dynasty"
                every { posterPath } returns "/succ_poster.jpg"
                every { backdropPath } returns "/succ_backdrop.jpg"
                every { voteAvg } returns 8.9f
                every { voteCount } returns 4000
                every { firstAirDate } returns "2018-06-03"
            }
            val pagedPayload = mockk<PagedPayload<RemoteTvShow>>(relaxed = true) {
                every { results } returns listOf(fakeTv)
            }

            coEvery {
                mockTmdbApiService.getDiscoveredTvShows(capture(capturedQueryMap), any())
            } returns ApiResponse.Success(body = pagedPayload, payload = mockk(relaxed = true))

            val filter = UniversalDiscoveryFilter(
                mediaType = MediaType.Tv,
                tvNetworkHub = DiscoveryTvNetworkHub.HBO,
                ratingThreshold = DiscoveryRatingThreshold.MASTERPIECE_8_PLUS,
                language = DiscoveryLanguage.ENGLISH,
                certification = "TV-MA",
                watchRegion = "US"
            )

            val result = repository.discoverUniversal(filter, page = 1)

            assertThat(result).isInstanceOf(Result.Success::class.java)
            val queryMap = capturedQueryMap.captured
            assertThat(queryMap["with_networks"]).isEqualTo("49") // HBO Network ID
            assertThat(queryMap["vote_average.gte"]).isEqualTo("8.0")
            assertThat(queryMap["with_original_language"]).isEqualTo("en")
            assertThat(queryMap["certification_country"]).isEqualTo("US")
            assertThat(queryMap["certification"]).isEqualTo("TV-MA")
        }
}
