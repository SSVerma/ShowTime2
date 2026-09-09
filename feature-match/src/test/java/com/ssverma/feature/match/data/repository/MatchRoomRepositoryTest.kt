package com.ssverma.feature.match.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.FirebaseFirestore
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.match.MatchDeckType
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.repository.AppConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MatchRoomRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockPrefs: SharedPreferences = mockk(relaxed = true)
    private val mockFirestore: FirebaseFirestore = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)

    private lateinit var repository: MatchRoomRepositoryImpl

    private val keyLastSessionDate = stringPreferencesKey("match_room_last_session_date")
    private val keyDailySessionCount = intPreferencesKey("match_room_daily_session_count")

    @Before
    fun setUp() {
        val appInfo = ApplicationInfo().apply { flags = 0 }
        every { mockContext.applicationInfo } returns appInfo
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.getString("persistent_user_uuid", any()) } returns "mock-user-123"

        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns flowOf(emptyPreferences())

        repository = MatchRoomRepositoryImpl(
            context = mockContext,
            firestore = mockFirestore,
            tmdbApiService = mockTmdbApiService,
            appConfigRepository = mockAppConfigRepository,
            watchlistDao = mockWatchlistDao,
            keyValueStorageClient = mockKeyValueStorageClient
        )
    }

    @Test
    fun `fetchMatchDeck with TRENDING returns mapped movie match cards`() = runTest {
        val fakeMovie1 = mockk<RemoteMovie>(relaxed = true) {
            every { id } returns 101
            every { title } returns "Inception"
            every { voteAvg } returns 8.8f
            every { posterPath } returns "/inception.jpg"
            every { backdropPath } returns "/inception_backdrop.jpg"
            every { releaseDate } returns "2010-07-16"
            every { overview } returns "A thief who steals corporate secrets..."
            every { runtime } returns 148
        }
        val fakeMovie2 = mockk<RemoteMovie>(relaxed = true) {
            every { id } returns 102
            every { title } returns "Interstellar"
            every { voteAvg } returns 8.6f
            every { posterPath } returns "/interstellar.jpg"
            every { backdropPath } returns "/interstellar_backdrop.jpg"
            every { releaseDate } returns "2014-11-07"
            every { overview } returns "When Earth becomes uninhabitable..."
            every { runtime } returns 169
        }

        val pagedPayload = mockk<PagedPayload<RemoteMovie>>(relaxed = true) {
            every { results } returns listOf(fakeMovie1, fakeMovie2)
        }

        coEvery {
            mockTmdbApiService.getTrendingMovies("day", 1)
        } returns ApiResponse.Success(body = pagedPayload, payload = mockk(relaxed = true))

        val result = repository.fetchMatchDeck(
            MatchRoomConfig(
                deckType = MatchDeckType.TRENDING,
                deckSize = 10
            )
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val cards = (result as Result.Success).data
        assertThat(cards).hasSize(2)
        assertThat(cards[0].id).isEqualTo(101)
        assertThat(cards[0].title).isEqualTo("Inception")
        assertThat(cards[0].releaseYear).isEqualTo("2010")
        assertThat(cards[0].runtime).isEqualTo(148)
        assertThat(cards[1].id).isEqualTo(102)
        assertThat(cards[1].title).isEqualTo("Interstellar")
        assertThat(cards[1].releaseYear).isEqualTo("2014")
    }

    @Test
    fun `fetchMatchDeck with empty response returns Error`() = runTest {
        val pagedPayload = mockk<PagedPayload<RemoteMovie>>(relaxed = true) {
            every { results } returns emptyList()
        }

        coEvery {
            mockTmdbApiService.getTrendingMovies("day", 1)
        } returns ApiResponse.Success(body = pagedPayload, payload = mockk(relaxed = true))

        val result = repository.fetchMatchDeck(MatchRoomConfig(deckType = MatchDeckType.TRENDING))

        assertThat(result).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun `fetchMatchDeck with GENRE passes genre filter to tmdb`() = runTest {
        val fakeMovie = mockk<RemoteMovie>(relaxed = true) {
            every { id } returns 201
            every { title } returns "Hereditary"
            every { voteAvg } returns 7.3f
            every { posterPath } returns "/hereditary.jpg"
            every { releaseDate } returns "2018-06-08"
        }

        val pagedPayload = mockk<PagedPayload<RemoteMovie>>(relaxed = true) {
            every { results } returns listOf(fakeMovie)
        }

        coEvery {
            mockTmdbApiService.getDiscoveredMovies(
                queryMap = match { it["with_genres"] == "27" },
                page = 1
            )
        } returns ApiResponse.Success(body = pagedPayload, payload = mockk(relaxed = true))

        val result = repository.fetchMatchDeck(
            MatchRoomConfig(deckType = MatchDeckType.GENRE, genreId = 27, deckSize = 15)
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val cards = (result as Result.Success).data
        assertThat(cards).hasSize(1)
        assertThat(cards[0].id).isEqualTo(201)
        assertThat(cards[0].title).isEqualTo("Hereditary")
    }

    @Test
    fun `canStartMatchSession returns true when Pro is active`() = runTest {
        val canStart = repository.canStartMatchSession(isProActive = true)
        assertThat(canStart).isTrue()
    }

    @Test
    fun `canStartMatchSession returns true when no sessions played today`() = runTest {
        every { mockStorage.data } returns flowOf(emptyPreferences())

        val canStart = repository.canStartMatchSession(isProActive = false)
        assertThat(canStart).isTrue()
    }

    @Test
    fun `canStartMatchSession returns false when daily session quota reached today`() = runTest {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = preferencesOf(
            keyLastSessionDate to today,
            keyDailySessionCount to 1
        )
        every { mockStorage.data } returns flowOf(prefs)

        val canStart = repository.canStartMatchSession(isProActive = false)
        assertThat(canStart).isFalse()
    }

    @Test
    fun `canStartMatchSession returns true when quota was reached on previous day`() = runTest {
        val prefs = preferencesOf(
            keyLastSessionDate to "2025-01-01",
            keyDailySessionCount to 1
        )
        every { mockStorage.data } returns flowOf(prefs)

        val canStart = repository.canStartMatchSession(isProActive = false)
        assertThat(canStart).isTrue()
    }

    @Test
    fun `saveMatchToWatchlist inserts card details into WatchlistDao`() = runTest {
        val card = MovieMatchCard(
            id = 550,
            title = "Fight Club",
            posterImageUrl = "https://image.tmdb.org/t/p/w500/fc.jpg",
            backdropImageUrl = "https://image.tmdb.org/t/p/w1280/fc_bd.jpg",
            releaseYear = "1999",
            voteAvg = 8.4f,
            overview = "An insomniac office worker...",
            genreNames = listOf("Drama"),
            watchProviders = emptyList(),
            runtime = 139
        )

        val result = repository.saveMatchToWatchlist(card)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify {
            mockWatchlistDao.insertWatchlist(
                match {
                    it.mediaId == 550 &&
                            it.mediaType == "movie" &&
                            it.title == "Fight Club" &&
                            it.releaseDate == "1999" &&
                            it.voteAvg == 8.4f
                }
            )
        }
    }
}
