package com.ssverma.shared.data.worker

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.RemoteProviderInfo
import com.ssverma.api.service.tmdb.response.RemoteWatchProvider
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderResponse
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.notifications.ShowTimeNotificationManager
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.release.ReleaseRadarConfig
import com.ssverma.shared.domain.repository.AppConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class ReleaseRadarProcessorTest {

    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockAppConfigProvider: AppConfigProvider = mockk(relaxed = true)
    private val mockNotificationManager: ShowTimeNotificationManager = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)

    private val watchProviderRegionFlow = MutableStateFlow("US")
    private val userSubscriptionsFlow = MutableStateFlow<Set<Int>>(emptySet())
    private val isNotificationsEnabledFlow = MutableStateFlow(true)
    private val isReleaseRadarEnabledFlow = MutableStateFlow(true)

    private lateinit var processor: ReleaseRadarProcessor

    private val testDate = LocalDate.of(2026, 9, 12)

    @Before
    fun setUp() {
        every {
            mockAppConfigProvider.getBoolean(
                ReleaseRadarConfig.REMOTE_KEY_RELEASE_RADAR_ENABLED,
                any()
            )
        } returns true
        every { mockAppConfigRepository.isNotificationsEnabled } returns isNotificationsEnabledFlow
        every { mockAppConfigRepository.isReleaseRadarEnabled } returns isReleaseRadarEnabledFlow
        every { mockAppConfigRepository.watchProviderRegion } returns watchProviderRegionFlow
        every { mockAppConfigRepository.userStreamingSubscriptions } returns userSubscriptionsFlow
        every { mockNotificationManager.hasNotificationPermission() } returns true

        every { mockContext.getString(any()) } returns "Mock notification message"
        every { mockContext.getString(any(), *anyVararg()) } answers {
            val title = args[1]
            "$title notification"
        }

        processor = ReleaseRadarProcessor(
            watchlistDao = mockWatchlistDao,
            tmdbApiService = mockTmdbApiService,
            appConfigRepository = mockAppConfigRepository,
            appConfigProvider = mockAppConfigProvider,
            notificationManager = mockNotificationManager,
            context = mockContext
        )
    }

    @Test
    fun `executeRadar when remote kill switch is disabled returns Success with zero queries`() =
        runTest {
            every {
                mockAppConfigProvider.getBoolean(
                    ReleaseRadarConfig.REMOTE_KEY_RELEASE_RADAR_ENABLED,
                    any()
                )
            } returns false

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            coVerify(exactly = 0) { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) }
            coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
        }

    @Test
    fun `executeRadar when notifications disabled returns Success with zero queries`() = runTest {
        isNotificationsEnabledFlow.value = false

        val result = processor.executeRadar(testDate)

        assertThat(result).isEqualTo(ReleaseRadarResult.Success)
        coVerify(exactly = 0) { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) }
        coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
    }

    @Test
    fun `executeRadar when release radar disabled returns Success with zero queries`() = runTest {
        isReleaseRadarEnabledFlow.value = false

        val result = processor.executeRadar(testDate)

        assertThat(result).isEqualTo(ReleaseRadarResult.Success)
        coVerify(exactly = 0) { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) }
        coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
    }

    @Test
    fun `executeRadar when notification permission missing returns Success with zero queries`() =
        runTest {
            every { mockNotificationManager.hasNotificationPermission() } returns false

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            coVerify(exactly = 0) { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) }
            coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
        }

    @Test
    fun `executeRadar with theatrical candidate triggers notification with zero API calls`() =
        runTest {
            val movie = WatchlistEntity(
                mediaId = 550,
                mediaType = "movie",
                title = "Fight Club",
                posterImageUrl = "/poster.jpg",
                backdropImageUrl = "/backdrop.jpg",
                voteAvg = 8.4f,
                releaseDate = "2026-09-12"
            )
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates("2026-09-12") } returns listOf(
                movie
            )

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            verify(exactly = 1) {
                mockNotificationManager.showReminderNotification(
                    title = any(),
                    message = any(),
                    imageUrl = "/poster.jpg",
                    deepLink = "showtime://showtime.ssverma.in/movie/550"
                )
            }
            coVerify(exactly = 1) { mockWatchlistDao.markTheatricalNotified(550) }
            coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
        }

    @Test
    fun `executeRadar with multiple theatrical candidates dispatches single notification and marks all notified`() =
        runTest {
            val movie1 = WatchlistEntity(
                mediaId = 550,
                mediaType = "movie",
                title = "Movie 1",
                posterImageUrl = "/poster1.jpg",
                backdropImageUrl = "",
                voteAvg = 8.0f,
                releaseDate = "2026-09-12"
            )
            val movie2 = WatchlistEntity(
                mediaId = 551,
                mediaType = "movie",
                title = "Movie 2",
                posterImageUrl = "/poster2.jpg",
                backdropImageUrl = "",
                voteAvg = 8.0f,
                releaseDate = "2026-09-12"
            )
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates("2026-09-12") } returns listOf(
                movie1,
                movie2
            )

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            verify(exactly = 1) {
                mockNotificationManager.showReminderNotification(
                    any(),
                    any(),
                    any(),
                    any()
                )
            }
            coVerify(exactly = 1) { mockWatchlistDao.markTheatricalNotified(550) }
            coVerify(exactly = 1) { mockWatchlistDao.markTheatricalNotified(551) }
        }

    @Test
    fun `executeRadar skips candidate with blank title`() = runTest {
        val blankTitleMovie = WatchlistEntity(
            mediaId = 550,
            mediaType = "movie",
            title = "   ",
            posterImageUrl = "/poster.jpg",
            backdropImageUrl = "",
            voteAvg = 8.0f,
            releaseDate = "2026-09-12"
        )
        coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates("2026-09-12") } returns listOf(
            blankTitleMovie
        )

        val result = processor.executeRadar(testDate)

        assertThat(result).isEqualTo(ReleaseRadarResult.Success)
        verify(exactly = 0) {
            mockNotificationManager.showReminderNotification(
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 1) { mockWatchlistDao.markTheatricalNotified(550) }
    }

    @Test
    fun `executeRadar when theatrical notification dispatched skips streaming checks`() = runTest {
        val theatricalMovie = WatchlistEntity(
            mediaId = 100,
            mediaType = "movie",
            title = "Dune 3",
            posterImageUrl = "/dune.jpg",
            backdropImageUrl = "",
            voteAvg = 8.5f,
            releaseDate = "2026-09-12"
        )
        coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates("2026-09-12") } returns listOf(
            theatricalMovie
        )

        val result = processor.executeRadar(testDate)

        assertThat(result).isEqualTo(ReleaseRadarResult.Success)
        coVerify(exactly = 0) {
            mockWatchlistDao.getStreamingRadarCandidates(
                any(),
                any(),
                any(),
                any()
            )
        }
        coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(any()) }
    }

    @Test
    fun `executeRadar with matching streaming candidate dispatches alert and updates status`() =
        runTest {
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) } returns emptyList()

            val streamingMovie = WatchlistEntity(
                mediaId = 200,
                mediaType = "movie",
                title = "Challengers",
                posterImageUrl = "/challengers.jpg",
                backdropImageUrl = "",
                voteAvg = 7.8f,
                releaseDate = "2026-07-15"
            )
            coEvery {
                mockWatchlistDao.getStreamingRadarCandidates(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(streamingMovie)

            userSubscriptionsFlow.value = setOf(8) // Netflix

            val netflix = RemoteProviderInfo(
                logoPath = null,
                providerId = 8,
                providerName = "Netflix",
                displayPriority = 1
            )
            val watchProvider = RemoteWatchProvider(
                link = "https://tmdb.org",
                flatRate = listOf(netflix),
                rent = null,
                buy = null,
                free = null,
                ads = null
            )
            val remoteResponse = RemoteWatchProviderResponse(results = mapOf("US" to watchProvider))
            coEvery { mockTmdbApiService.getMovieWatchProviders(200) } returns ApiResponse.Success(
                body = remoteResponse,
                payload = mockk(relaxed = true)
            )

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            verify(exactly = 1) {
                mockNotificationManager.showReminderNotification(
                    title = any(),
                    message = any(),
                    imageUrl = "/challengers.jpg",
                    deepLink = "showtime://showtime.ssverma.in/movie/200"
                )
            }
            coVerify(exactly = 1) {
                mockWatchlistDao.updateStreamingCheckStatus(
                    mediaId = 200,
                    checkEpochMs = any(),
                    providers = "Netflix",
                    hasNotified = true
                )
            }
        }

    @Test
    fun `executeRadar when candidate not on user subscriptions does not notify and enforces 7-day cooldown`() =
        runTest {
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) } returns emptyList()

            val streamingMovie = WatchlistEntity(
                mediaId = 200,
                mediaType = "movie",
                title = "Challengers",
                posterImageUrl = "/challengers.jpg",
                backdropImageUrl = "",
                voteAvg = 7.8f,
                releaseDate = "2026-07-15"
            )
            coEvery {
                mockWatchlistDao.getStreamingRadarCandidates(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(streamingMovie)

            userSubscriptionsFlow.value = setOf(8) // User only subscribes to Netflix

            val prime = RemoteProviderInfo(
                logoPath = null,
                providerId = 119,
                providerName = "Prime Video",
                displayPriority = 1
            )
            val watchProvider = RemoteWatchProvider(
                link = "https://tmdb.org",
                flatRate = listOf(prime),
                rent = null,
                buy = null,
                free = null,
                ads = null
            )
            val remoteResponse = RemoteWatchProviderResponse(results = mapOf("US" to watchProvider))
            coEvery { mockTmdbApiService.getMovieWatchProviders(200) } returns ApiResponse.Success(
                body = remoteResponse,
                payload = mockk(relaxed = true)
            )

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            verify(exactly = 0) {
                mockNotificationManager.showReminderNotification(
                    any(),
                    any(),
                    any(),
                    any()
                )
            }
            coVerify(exactly = 1) {
                mockWatchlistDao.updateStreamingCheckStatus(
                    mediaId = 200,
                    checkEpochMs = any(),
                    providers = "",
                    hasNotified = false
                )
            }
        }

    @Test
    fun `executeRadar when movie only available on rent or buy does not trigger streaming alert`() =
        runTest {
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) } returns emptyList()

            val movie = WatchlistEntity(
                mediaId = 300,
                mediaType = "movie",
                title = "Furiosa",
                posterImageUrl = "/furiosa.jpg",
                backdropImageUrl = "",
                voteAvg = 7.6f,
                releaseDate = "2026-07-01"
            )
            coEvery {
                mockWatchlistDao.getStreamingRadarCandidates(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(movie)

            val appleBuy = RemoteProviderInfo(
                logoPath = null,
                providerId = 2,
                providerName = "Apple TV",
                displayPriority = 1
            )
            val watchProvider = RemoteWatchProvider(
                link = "https://tmdb.org",
                flatRate = null,
                rent = listOf(appleBuy),
                buy = listOf(appleBuy),
                free = null,
                ads = null
            )
            val remoteResponse = RemoteWatchProviderResponse(results = mapOf("US" to watchProvider))
            coEvery { mockTmdbApiService.getMovieWatchProviders(300) } returns ApiResponse.Success(
                body = remoteResponse,
                payload = mockk(relaxed = true)
            )

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            verify(exactly = 0) {
                mockNotificationManager.showReminderNotification(
                    any(),
                    any(),
                    any(),
                    any()
                )
            }
            coVerify(exactly = 1) {
                mockWatchlistDao.updateStreamingCheckStatus(
                    mediaId = 300,
                    checkEpochMs = any(),
                    providers = "",
                    hasNotified = false
                )
            }
        }

    @Test
    fun `executeRadar with malformed release date handles safely without querying TMDB`() =
        runTest {
            coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) } returns emptyList()

            val badDateMovie = WatchlistEntity(
                mediaId = 400,
                mediaType = "movie",
                title = "Corrupted Date Movie",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 6.0f,
                releaseDate = "invalid-date-string"
            )
            coEvery {
                mockWatchlistDao.getStreamingRadarCandidates(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(badDateMovie)

            val result = processor.executeRadar(testDate)

            assertThat(result).isEqualTo(ReleaseRadarResult.Success)
            coVerify(exactly = 0) { mockTmdbApiService.getMovieWatchProviders(400) }
            coVerify(exactly = 1) {
                mockWatchlistDao.updateStreamingCheckStatus(
                    mediaId = 400,
                    checkEpochMs = any(),
                    providers = "",
                    hasNotified = false
                )
            }
        }

    @Test
    fun `executeRadar when TMDB API throws network exception does not crash`() = runTest {
        coEvery { mockWatchlistDao.getTheatricalReleaseAlertCandidates(any()) } returns emptyList()

        val movie = WatchlistEntity(
            mediaId = 500,
            mediaType = "movie",
            title = "Offline Movie",
            posterImageUrl = "",
            backdropImageUrl = "",
            voteAvg = 7.0f,
            releaseDate = "2026-07-20"
        )
        coEvery {
            mockWatchlistDao.getStreamingRadarCandidates(
                any(),
                any(),
                any(),
                any()
            )
        } returns listOf(movie)
        coEvery { mockTmdbApiService.getMovieWatchProviders(500) } throws IOException("Socket timeout")

        val result = processor.executeRadar(testDate)

        assertThat(result).isEqualTo(ReleaseRadarResult.Success)
        verify(exactly = 0) {
            mockNotificationManager.showReminderNotification(
                any(),
                any(),
                any(),
                any()
            )
        }
    }
}
