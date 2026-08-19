package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.domain.model.MediaType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LibraryRepositoryTest {

    private val mockFavoriteDao: FavoriteDao = mockk(relaxed = true)
    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockWatchHistoryDao: WatchHistoryDao = mockk(relaxed = true)
    private val mockCustomListDao: CustomListDao = mockk(relaxed = true)

    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setUp() {
        repository = LibraryRepositoryImpl(
            favoriteDao = mockFavoriteDao,
            watchlistDao = mockWatchlistDao,
            watchHistoryDao = mockWatchHistoryDao,
            customListDao = mockCustomListDao
        )
    }

    @Test
    fun `toggleFavorite adds to database when not currently favorite`() = runTest {
        coEvery { mockFavoriteDao.isFavorite(101) } returns false

        val isAdded = repository.toggleFavorite(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inception.jpg",
            backdropImageUrl = "/backdrop.jpg",
            voteAvg = 8.8f,
            releaseDate = "2010-07-16"
        )

        assertThat(isAdded).isTrue()
        coVerify {
            mockFavoriteDao.insertFavorite(
                match { it.mediaId == 101 && it.title == "Inception" && it.mediaType == "movie" }
            )
        }
    }

    @Test
    fun `toggleFavorite removes from database when already favorite`() = runTest {
        coEvery { mockFavoriteDao.isFavorite(101) } returns true

        val isAdded = repository.toggleFavorite(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inception.jpg",
            backdropImageUrl = "/backdrop.jpg",
            voteAvg = 8.8f,
            releaseDate = "2010-07-16"
        )

        assertThat(isAdded).isFalse()
        coVerify { mockFavoriteDao.deleteFavoriteById(101) }
    }

    @Test
    fun `toggleWatchlist adds to database when not in watchlist`() = runTest {
        coEvery { mockWatchlistDao.isInWatchlist(202) } returns false

        val isAdded = repository.toggleWatchlist(
            mediaId = 202,
            mediaType = MediaType.Tv,
            title = "Dark",
            posterImageUrl = "/dark.jpg",
            backdropImageUrl = "/dark_bg.jpg",
            voteAvg = 8.7f,
            releaseDate = "2017-12-01"
        )

        assertThat(isAdded).isTrue()
        coVerify {
            mockWatchlistDao.insertWatchlist(
                match { it.mediaId == 202 && it.title == "Dark" && it.mediaType == "tv" }
            )
        }
    }

    @Test
    fun `logWatchHistory inserts history entry into database`() = runTest {
        repository.logWatchHistory(
            mediaId = 303,
            mediaType = MediaType.Movie,
            title = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg",
            voteAvg = 8.9f
        )

        coVerify {
            mockWatchHistoryDao.insertHistory(
                match { it.mediaId == 303 && it.title == "Oppenheimer" }
            )
        }
    }

    @Test
    fun `createCustomList inserts list into database`() = runTest {
        val listId = repository.createCustomList(
            title = "Mind-Bending Movies",
            description = "Complex plotlines"
        )

        assertThat(listId).isNotEmpty()
        coVerify {
            mockCustomListDao.insertList(
                match { it.title == "Mind-Bending Movies" && it.description == "Complex plotlines" }
            )
        }
    }

    @Test
    fun `deleteCustomList deletes list from database`() = runTest {
        repository.deleteCustomList("list-123")
        coVerify { mockCustomListDao.deleteListById("list-123") }
    }

    @Test
    fun `addMediaToCustomList inserts custom list item`() = runTest {
        repository.addMediaToCustomList(
            listId = "list-123",
            mediaId = 555,
            mediaType = MediaType.Movie,
            title = "Memento",
            posterImageUrl = "/memento.jpg"
        )

        coVerify {
            mockCustomListDao.insertListItem(
                match { it.listId == "list-123" && it.mediaId == 555 && it.title == "Memento" }
            )
        }
    }
}
