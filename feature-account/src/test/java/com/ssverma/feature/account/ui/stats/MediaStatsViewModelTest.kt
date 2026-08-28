package com.ssverma.feature.account.ui.stats

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaStatsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var viewModel: MediaStatsViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        viewModel = MediaStatsViewModel(libraryRepository = fakeLibraryRepository)
    }

    @Test
    fun `fetchMediaStats returns current favorite and watchlist status from room db`() = runTest {
        fakeLibraryRepository.toggleFavorite(
            mediaId = 42,
            mediaType = MediaType.Movie,
            title = "The Matrix",
            posterImageUrl = "/matrix.jpg",
            backdropImageUrl = "",
            voteAvg = 8.7f,
            releaseDate = "1999-03-31"
        )

        viewModel.fetchMediaStats(mediaType = MediaType.Movie, mediaId = 42)

        viewModel.mediaStats.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(MediaStatsUiState.Success::class.java)
            val stats = (state as MediaStatsUiState.Success).mediaStats
            assertThat(stats.mediaId).isEqualTo(42)
            assertThat(stats.favorite).isTrue()
            assertThat(stats.inWatchlist).isFalse()
        }
    }

    @Test
    fun `toggleMediaFavoriteStatus toggles favorite in room db and updates state`() = runTest {
        viewModel.toggleMediaFavoriteStatus(
            mediaType = MediaType.Movie,
            mediaId = 100,
            title = "Dune",
            posterImageUrl = "/dune.jpg"
        )

        viewModel.mediaStats.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(MediaStatsUiState.Success::class.java)
            val stats = (state as MediaStatsUiState.Success).mediaStats
            assertThat(stats.favorite).isTrue()
        }

        assertThat(fakeLibraryRepository.isFavorite(100)).isTrue()
    }

    @Test
    fun `toggleMediaWatchlistStatus toggles watchlist in room db and updates state`() = runTest {
        viewModel.toggleMediaWatchlistStatus(
            mediaType = MediaType.Movie,
            mediaId = 200,
            title = "Blade Runner 2049",
            posterImageUrl = "/bladerunner.jpg"
        )

        viewModel.mediaStats.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(MediaStatsUiState.Success::class.java)
            val stats = (state as MediaStatsUiState.Success).mediaStats
            assertThat(stats.inWatchlist).isTrue()
        }

        assertThat(fakeLibraryRepository.isInWatchlist(200)).isTrue()
    }

    @Test
    fun `toggleWatchHistoryStatus toggles watched history in room db and updates state`() =
        runTest {
            viewModel.toggleWatchHistoryStatus(
                mediaType = MediaType.Movie,
                mediaId = 300,
                title = "Oppenheimer",
                posterImageUrl = "/oppenheimer.jpg"
            )

            viewModel.mediaStats.test {
                val state = awaitItem()
                assertThat(state).isInstanceOf(MediaStatsUiState.Success::class.java)
                val stats = (state as MediaStatsUiState.Success).mediaStats
                assertThat(stats.isWatched).isTrue()
            }

            assertThat(fakeLibraryRepository.isWatched(300)).isTrue()

            // Toggle again to remove
            viewModel.toggleWatchHistoryStatus(
                mediaType = MediaType.Movie,
                mediaId = 300,
                title = "Oppenheimer",
                posterImageUrl = "/oppenheimer.jpg"
            )

            viewModel.mediaStats.test {
                val state = awaitItem()
                assertThat(state).isInstanceOf(MediaStatsUiState.Success::class.java)
                val stats = (state as MediaStatsUiState.Success).mediaStats
                assertThat(stats.isWatched).isFalse()
            }

            assertThat(fakeLibraryRepository.isWatched(300)).isFalse()
        }

    @Test
    fun `isMediaActionActiveFlow emits true when media is in favorites, watchlist, or history`() = runTest {
        viewModel.isMediaActionActiveFlow(500).test {
            // Initially false
            assertThat(awaitItem()).isFalse()

            // Add to favorites -> true
            fakeLibraryRepository.toggleFavorite(
                mediaId = 500,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 8.6f,
                releaseDate = "2014-11-05"
            )
            assertThat(awaitItem()).isTrue()

            // Remove from favorites -> false
            fakeLibraryRepository.deleteFavorite(500)
            assertThat(awaitItem()).isFalse()

            // Add to watchlist -> true
            fakeLibraryRepository.toggleWatchlist(
                mediaId = 500,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 8.6f,
                releaseDate = "2014-11-05"
            )
            assertThat(awaitItem()).isTrue()
        }
    }
}
