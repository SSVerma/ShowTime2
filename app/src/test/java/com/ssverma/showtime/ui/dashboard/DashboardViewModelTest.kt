package com.ssverma.showtime.ui.dashboard

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import com.ssverma.shared.testing.fakes.FakeTraktSyncRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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

    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)

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
        every { adConfigProvider.isAdsEnabled } returns false

        every { traktAuthManager.authState } returns traktAuthFlow

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
            traktSyncRepository = fakeTraktSyncRepository
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
}
