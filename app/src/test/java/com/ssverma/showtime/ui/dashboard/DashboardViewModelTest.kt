package com.ssverma.showtime.ui.dashboard

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.movie.domain.usecase.MovieGenresUseCase
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TvGenresUseCase
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.domain.Result
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.model.auth.TraktAuthState
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import com.ssverma.shared.domain.usecase.community.GetDailyPollUseCase
import com.ssverma.shared.domain.usecase.community.GetTrendingDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.VoteDailyPollUseCase
import com.ssverma.shared.testing.fakes.FakeTraktSyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trendingMoviesUseCase: TrendingMoviesUseCase = mockk(relaxed = true)
    private val trendingTvShowsUseCase: TrendingTvShowsUseCase = mockk(relaxed = true)
    private val popularMoviesUseCase: PopularMoviesUseCase = mockk(relaxed = true)
    private val popularTvShowsUseCase: PopularTvShowsUseCase = mockk(relaxed = true)
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val adConfigProvider: AdConfigProvider = mockk(relaxed = true)
    private val cinemaGameRepository: CinemaGameRepository = mockk(relaxed = true)
    private val traktAuthManager: TraktAuthManager = mockk(relaxed = true)
    private val fakeTraktSyncRepository = FakeTraktSyncRepository()
    private val getDailyPollUseCase: GetDailyPollUseCase = mockk(relaxed = true)
    private val voteDailyPollUseCase: VoteDailyPollUseCase = mockk(relaxed = true)
    private val getTrendingDiscussionsUseCase: GetTrendingDiscussionsUseCase = mockk(relaxed = true)
    private val movieGenresUseCase: MovieGenresUseCase = mockk(relaxed = true)
    private val tvGenresUseCase: TvGenresUseCase = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val billingRepository: BillingRepository = mockk(relaxed = true)

    private val isProActiveFlow = MutableStateFlow(false)
    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)
    private val notificationShelfDismissedFlow = MutableStateFlow(0L)

    private val sampleUpNextEpisode = TraktUpNextEpisode(
        showTmdbId = 1396,
        showTitle = "Breaking Bad",
        showPosterPath = "poster.jpg",
        seasonNumber = 2,
        episodeNumber = 4,
        episodeTitle = "Down",
        totalCompleted = 10,
        totalAired = 62
    )

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")
        every { appConfigRepository.isTranslationEnabled } returns MutableStateFlow(false)
        every { appConfigRepository.contentLanguage } returns MutableStateFlow("en")
        every { appConfigRepository.preferredOriginalLanguage } returns MutableStateFlow("en")
        every { appConfigRepository.notificationShelfLastDismissedMs } returns notificationShelfDismissedFlow
        coEvery { appConfigRepository.dismissNotificationShelf() } coAnswers {
            notificationShelfDismissedFlow.value = System.currentTimeMillis()
        }
        every { adConfigProvider.isAdsEnabled } returns false

        every { traktAuthManager.authState } returns traktAuthFlow
        every { getDailyPollUseCase(any()) } returns MutableStateFlow(
            DailyPoll.empty(LocalDate.now())
        )
        every { getTrendingDiscussionsUseCase() } returns MutableStateFlow(emptyList())

        coEvery { trendingMoviesUseCase(any()) } returns Result.Success(emptyList())
        coEvery { trendingTvShowsUseCase(any()) } returns Result.Success(emptyList())
        coEvery { popularMoviesUseCase() } returns Result.Success(emptyList())
        coEvery { popularTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { fetchAllWatchProvidersUseCase.fetchMovieWatchProviders() } returns Result.Success(
            emptyList()
        )
        coEvery { fetchAllWatchProvidersUseCase.fetchTvWatchProviders() } returns Result.Success(
            emptyList()
        )
        coEvery { movieGenresUseCase() } returns Result.Success(emptyList())
        coEvery { tvGenresUseCase() } returns Result.Success(emptyList())

        every { billingRepository.isProActive } returns isProActiveFlow

        viewModel = DashboardViewModel(
            trendingMoviesUseCase = trendingMoviesUseCase,
            trendingTvShowsUseCase = trendingTvShowsUseCase,
            popularMoviesUseCase = popularMoviesUseCase,
            popularTvShowsUseCase = popularTvShowsUseCase,
            fetchAllWatchProvidersUseCase = fetchAllWatchProvidersUseCase,
            appConfigRepository = appConfigRepository,
            adConfigProvider = adConfigProvider,
            cinemaGameRepository = cinemaGameRepository,
            traktAuthManager = traktAuthManager,
            traktSyncRepository = fakeTraktSyncRepository,
            getDailyPollUseCase = getDailyPollUseCase,
            voteDailyPollUseCase = voteDailyPollUseCase,
            getTrendingDiscussionsUseCase = getTrendingDiscussionsUseCase,
            movieGenresUseCase = movieGenresUseCase,
            tvGenresUseCase = tvGenresUseCase,
            reminderRepository = reminderRepository,
            billingRepository = billingRepository
        )
    }

    @Test
    fun `markEpisodeWatched optimistically increments totalCompleted and preserves totalAired`() =
        runTest {
            traktAuthFlow.value = TraktAuthState.Connected(
                user = mockk(relaxed = true),
                accessToken = "test_token"
            )
            fakeTraktSyncRepository.upNextQueueFlow.value = listOf(sampleUpNextEpisode)
            advanceUntilIdle()

            viewModel.markEpisodeWatched(
                showTmdbId = 1396,
                season = 2,
                episode = 4
            )

            // Verify optimistic update: totalCompleted becomes 11, totalAired stays 62
            val state = viewModel.uiState.value
            val updated = state.upNextQueue.first { it.showTmdbId == 1396 }
            assertThat(updated.totalCompleted).isEqualTo(11)
            assertThat(updated.totalAired).isEqualTo(62)
            assertThat(updated.episodeNumber).isEqualTo(5)

            advanceUntilIdle()

            assertThat(fakeTraktSyncRepository.lastMarkedShowTmdbId).isEqualTo(1396)
            assertThat(fakeTraktSyncRepository.lastMarkedSeason).isEqualTo(2)
            assertThat(fakeTraktSyncRepository.lastMarkedEpisode).isEqualTo(4)
            assertThat(fakeTraktSyncRepository.lastMarkedShowTitle).isEqualTo("Breaking Bad")
            assertThat(fakeTraktSyncRepository.lastMarkedPosterPath).isEqualTo("poster.jpg")
            assertThat(fakeTraktSyncRepository.lastMarkedTotalAired).isEqualTo(62)
            // Episode title is updated from repository emission
            assertThat(viewModel.uiState.value.upNextQueue.first().episodeTitle).isEqualTo("Episode 5")
        }

    @Test
    fun `markEpisodeWatched on final episode triggers completion dialog and dismiss removes card`() =
        runTest {
            val nearCompleteShow = sampleUpNextEpisode.copy(
                totalCompleted = 61,
                totalAired = 62,
                seasonNumber = 5,
                episodeNumber = 16
            )
            traktAuthFlow.value = TraktAuthState.Connected(
                user = mockk(relaxed = true),
                accessToken = "test_token"
            )
            fakeTraktSyncRepository.upNextQueueFlow.value = listOf(nearCompleteShow)
            advanceUntilIdle()

            viewModel.markEpisodeWatched(
                showTmdbId = 1396,
                season = 5,
                episode = 16
            )

            // Verify completion dialog is triggered
            val stateWithDialog = viewModel.uiState.value
            assertThat(stateWithDialog.completedShowDialog).isNotNull()
            assertThat(stateWithDialog.completedShowDialog?.showTmdbId).isEqualTo(1396)
            assertThat(stateWithDialog.completedShowDialog?.showTitle).isEqualTo("Breaking Bad")
            assertThat(stateWithDialog.completedShowDialog?.totalCompleted).isEqualTo(62)

            // Dismiss dialog
            viewModel.dismissCompletedShowDialog()
            val stateAfterDismiss = viewModel.uiState.value
            assertThat(stateAfterDismiss.completedShowDialog).isNull()
            assertThat(stateAfterDismiss.upNextQueue).isEmpty()
        }

    @Test
    fun `setMovieGenreSelected updates genre selection state`() {
        assertThat(viewModel.uiState.value.isMovieGenreSelected).isTrue()

        viewModel.setMovieGenreSelected(false)
        assertThat(viewModel.uiState.value.isMovieGenreSelected).isFalse()

        viewModel.setMovieGenreSelected(true)
        assertThat(viewModel.uiState.value.isMovieGenreSelected).isTrue()
    }

    @Test
    fun `openDailyPollSheet and dismissDailyPollSheet toggle sheet visibility`() {
        assertThat(viewModel.uiState.value.showDailyPollSheet).isFalse()

        viewModel.openDailyPollSheet()
        assertThat(viewModel.uiState.value.showDailyPollSheet).isTrue()

        viewModel.dismissDailyPollSheet()
        assertThat(viewModel.uiState.value.showDailyPollSheet).isFalse()
    }

    @Test
    fun `setMovieStudioSelected updates studio selection state`() {
        assertThat(viewModel.uiState.value.isMovieStudioSelected).isTrue()

        viewModel.setMovieStudioSelected(false)
        assertThat(viewModel.uiState.value.isMovieStudioSelected).isFalse()

        viewModel.setMovieStudioSelected(true)
        assertThat(viewModel.uiState.value.isMovieStudioSelected).isTrue()
    }

    @Test
    fun `notification shelf is not cooling down by default`() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isNotificationShelfCoolingDown).isFalse()
    }

    @Test
    fun `notification shelf is cooling down when dismissed within 7 days`() = runTest {
        notificationShelfDismissedFlow.value =
            System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isNotificationShelfCoolingDown).isTrue()
    }

    @Test
    fun `notification shelf is not cooling down when dismissed more than 7 days ago`() = runTest {
        notificationShelfDismissedFlow.value =
            System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isNotificationShelfCoolingDown).isFalse()
    }

    @Test
    fun `dismissNotificationShelf delegates to appConfigRepository`() = runTest {
        viewModel.dismissNotificationShelf()
        advanceUntilIdle()
        coVerify(exactly = 1) { appConfigRepository.dismissNotificationShelf() }
        assertThat(viewModel.uiState.value.isNotificationShelfCoolingDown).isTrue()
    }

    @Test
    fun `ad injection updates dynamically when pro status changes`() = runTest {
        every { adConfigProvider.isAdsEnabled } returns true
        val movie1 = mockk<Movie>(relaxed = true) { every { id } returns 1 }
        val movie2 = mockk<Movie>(relaxed = true) { every { id } returns 2 }
        val tv1 = mockk<TvShow>(relaxed = true) { every { id } returns 101 }
        val tv2 = mockk<TvShow>(relaxed = true) { every { id } returns 102 }

        coEvery { trendingMoviesUseCase(any()) } returns Result.Success(listOf(movie1, movie2))
        coEvery { trendingTvShowsUseCase(any()) } returns Result.Success(listOf(tv1, tv2))
        coEvery { popularMoviesUseCase() } returns Result.Success(listOf(movie1, movie2))
        coEvery { popularTvShowsUseCase() } returns Result.Success(listOf(tv1, tv2))

        viewModel.fetchTrendingMedia()
        viewModel.fetchPopularMovies()
        viewModel.fetchPopularTvShows()
        advanceUntilIdle()

        // Verify that ads are injected initially when isProActive = false and ads enabled
        val trendingWithAds = (viewModel.uiState.value.trendingMedia as UiState.Success).data
        val popularMoviesWithAds = (viewModel.uiState.value.popularMovies as UiState.Success).data
        val popularTvWithAds = (viewModel.uiState.value.popularTvShows as UiState.Success).data

        assertThat(trendingWithAds.any { it is InjectableAd }).isTrue()
        assertThat(popularMoviesWithAds.any { it is InjectableAd }).isTrue()
        assertThat(popularTvWithAds.any { it is InjectableAd }).isTrue()

        // Activate Pro
        isProActiveFlow.value = true
        advanceUntilIdle()

        // Verify that ads are removed when Pro is active
        val trendingNoAds = (viewModel.uiState.value.trendingMedia as UiState.Success).data
        val popularMoviesNoAds = (viewModel.uiState.value.popularMovies as UiState.Success).data
        val popularTvNoAds = (viewModel.uiState.value.popularTvShows as UiState.Success).data

        assertThat(trendingNoAds.any { it is InjectableAd }).isFalse()
        assertThat(popularMoviesNoAds.any { it is InjectableAd }).isFalse()
        assertThat(popularTvNoAds.any { it is InjectableAd }).isFalse()

        // Pro deactivated / expired
        isProActiveFlow.value = false
        advanceUntilIdle()

        // Verify that ads are re-injected
        val trendingAdsRestored = (viewModel.uiState.value.trendingMedia as UiState.Success).data
        val popularMoviesAdsRestored =
            (viewModel.uiState.value.popularMovies as UiState.Success).data
        val popularTvAdsRestored = (viewModel.uiState.value.popularTvShows as UiState.Success).data

        assertThat(trendingAdsRestored.any { it is InjectableAd }).isTrue()
        assertThat(popularMoviesAdsRestored.any { it is InjectableAd }).isTrue()
        assertThat(popularTvAdsRestored.any { it is InjectableAd }).isTrue()
    }
}

