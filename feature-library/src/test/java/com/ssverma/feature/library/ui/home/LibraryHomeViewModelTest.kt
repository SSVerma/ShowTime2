package com.ssverma.feature.library.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LibraryHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var viewModel: LibraryHomeViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        viewModel = LibraryHomeViewModel(libraryRepository = fakeLibraryRepository)
    }

    @Test
    fun `watchlistItems reflects added and removed watchlist items`() = runTest {
        viewModel.watchlistItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleWatchlist(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/inception.jpg",
                backdropImageUrl = "/backdrop.jpg",
                voteAvg = 8.8f,
                releaseDate = "2010-07-16"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Inception")
            assertThat(updated.first().mediaId).isEqualTo(101)

            viewModel.removeFromWatchlist(101)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `favoriteItems reflects added and removed favorite items`() = runTest {
        viewModel.favoriteItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleFavorite(
                mediaId = 202,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                backdropImageUrl = "/backdrop2.jpg",
                voteAvg = 8.6f,
                releaseDate = "2014-11-07"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Interstellar")

            viewModel.removeFromFavorites(202)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `historyItems reflects logged history and clear action`() = runTest {
        viewModel.historyItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.logWatchHistory(
                mediaId = 303,
                mediaType = MediaType.Movie,
                title = "Oppenheimer",
                posterImageUrl = "/oppenheimer.jpg",
                voteAvg = 8.9f
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Oppenheimer")

            viewModel.clearHistory()
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `filter state updates correctly`() = runTest {
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.ALL)
        viewModel.setWatchlistFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.MOVIE)

        viewModel.setFavoritesFilter(MediaTypeFilter.TV)
        assertThat(viewModel.favoritesFilter.value).isEqualTo(MediaTypeFilter.TV)

        viewModel.setHistoryFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.historyFilter.value).isEqualTo(MediaTypeFilter.MOVIE)
    }

    @Test
    fun `custom lists create, select, and delete work as expected`() = runTest {
        viewModel.customLists.test {
            assertThat(awaitItem()).isEmpty()

            var createdListId = ""
            viewModel.createCustomList(
                title = "Sci-Fi Masterpieces",
                description = "Mind bending movies",
                onCreated = { createdListId = it }
            )

            val lists = awaitItem()
            assertThat(lists).hasSize(1)
            assertThat(lists.first().title).isEqualTo("Sci-Fi Masterpieces")

            viewModel.selectCustomList(lists.first().listId)
            assertThat(viewModel.selectedCustomListId.value).isEqualTo(lists.first().listId)

            viewModel.deleteCustomList(lists.first().listId)
            val emptyLists = awaitItem()
            assertThat(emptyLists).isEmpty()
            assertThat(viewModel.selectedCustomListId.value).isNull()
        }
    }
}
