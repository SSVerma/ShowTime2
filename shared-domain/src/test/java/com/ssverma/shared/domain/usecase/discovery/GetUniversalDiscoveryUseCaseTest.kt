package com.ssverma.shared.domain.usecase.discovery

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetUniversalDiscoveryUseCaseTest {

    private val mockDiscoveryRepository: DiscoveryRepository = mockk()
    private val mockLibraryRepository: LibraryRepository = mockk()

    private lateinit var useCase: GetUniversalDiscoveryUseCase

    @Before
    fun setUp() {
        useCase = GetUniversalDiscoveryUseCase(
            discoveryRepository = mockDiscoveryRepository,
            libraryRepository = mockLibraryRepository
        )
    }

    @Test
    fun `invoke excludes watched titles when hideWatched is true`() = runTest {
        val filter = UniversalDiscoveryFilter(
            mediaType = MediaType.Movie,
            vibePreset = DiscoveryVibePreset.MIND_BENDING,
            hideWatched = true
        )

        val rawItems = listOf(
            UniversalMediaItem(
                id = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                overview = "Dream within a dream",
                posterImageUrl = "/inc.jpg",
                backdropImageUrl = "/inc_b.jpg",
                voteAvg = 8.8f,
                voteCount = 35000,
                releaseDate = "2010-07-16"
            ),
            UniversalMediaItem(
                id = 102,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                overview = "Wormhole journey",
                posterImageUrl = "/int.jpg",
                backdropImageUrl = "/int_b.jpg",
                voteAvg = 8.7f,
                voteCount = 32000,
                releaseDate = "2014-11-07"
            )
        )

        val watchedList = listOf(
            SavedMediaItem(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/inc.jpg",
                backdropImageUrl = "/inc_b.jpg",
                voteAvg = 8.8f,
                releaseDate = "2010-07-16",
                addedAt = 0L
            )
        )

        coEvery { mockDiscoveryRepository.discoverUniversal(filter, 1) } returns Result.Success(
            rawItems
        )
        coEvery { mockLibraryRepository.getAllWatchHistory() } returns flowOf(watchedList)
        coEvery { mockLibraryRepository.getAllFavorites() } returns flowOf(emptyList())
        coEvery { mockLibraryRepository.getAllWatchlist() } returns flowOf(emptyList())

        val result = useCase(filter, page = 1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val items = (result as Result.Success).data
        assertThat(items).hasSize(1)
        assertThat(items.first().id).isEqualTo(102)
        assertThat(items.first().title).isEqualTo("Interstellar")
    }

    @Test
    fun `invoke enriches items with favorite and watchlist flags`() = runTest {
        val filter = UniversalDiscoveryFilter(
            mediaType = MediaType.Movie,
            hideWatched = false
        )

        val rawItems = listOf(
            UniversalMediaItem(
                id = 201,
                mediaType = MediaType.Movie,
                title = "Dark",
                overview = "Time travel",
                posterImageUrl = "/dark.jpg",
                backdropImageUrl = "/dark_b.jpg",
                voteAvg = 8.9f,
                voteCount = 12000,
                releaseDate = "2017-12-01"
            )
        )

        val favoriteList = listOf(
            SavedMediaItem(
                mediaId = 201,
                mediaType = MediaType.Movie,
                title = "Dark",
                posterImageUrl = "/dark.jpg",
                backdropImageUrl = "/dark_b.jpg",
                voteAvg = 8.9f,
                releaseDate = "2017-12-01",
                addedAt = 0L
            )
        )

        coEvery { mockDiscoveryRepository.discoverUniversal(filter, 1) } returns Result.Success(
            rawItems
        )
        coEvery { mockLibraryRepository.getAllWatchHistory() } returns flowOf(emptyList())
        coEvery { mockLibraryRepository.getAllFavorites() } returns flowOf(favoriteList)
        coEvery { mockLibraryRepository.getAllWatchlist() } returns flowOf(emptyList())

        val result = useCase(filter, page = 1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val items = (result as Result.Success).data
        assertThat(items.first().isFavorite).isTrue()
        assertThat(items.first().isInWatchlist).isFalse()
    }
}
