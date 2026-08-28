package com.ssverma.feature.tv.ui.home

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.ui.UiState
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.tv.domain.usecase.NowAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.PopularTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TodayAiringTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TopRatedTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TrendingTvShowsUseCase
import com.ssverma.feature.tv.domain.usecase.TvGenresUseCase
import com.ssverma.feature.tv.domain.usecase.UpcomingTvShowsUseCase
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
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
class HomeTvShowViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trendingTvShowsUseCase: TrendingTvShowsUseCase = mockk(relaxed = true)
    private val topRatedTvShowsUseCase: TopRatedTvShowsUseCase = mockk(relaxed = true)
    private val upcomingTvShowsUseCase: UpcomingTvShowsUseCase = mockk(relaxed = true)
    private val todayAiringTvShowsUseCase: TodayAiringTvShowsUseCase = mockk(relaxed = true)
    private val nowAiringTvShowsUseCase: NowAiringTvShowsUseCase = mockk(relaxed = true)
    private val popularTvShowsUseCase: PopularTvShowsUseCase = mockk(relaxed = true)
    private val tvGenresUseCase: TvGenresUseCase = mockk(relaxed = true)
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val adConfigProvider: AdConfigProvider = mockk(relaxed = true)
    private val traktAuthManager: TraktAuthManager = mockk(relaxed = true)
    private val fakeTraktSyncRepository = FakeTraktSyncRepository()

    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)

    private val sampleUpNextEpisode = TraktUpNextEpisode(
        showTmdbId = 93405,
        showTitle = "Severance",
        showPosterPath = "poster.jpg",
        seasonNumber = 2,
        episodeNumber = 1,
        episodeTitle = "Hello, Innie",
        totalCompleted = 6,
        totalAired = 9
    )

    private lateinit var viewModel: HomeTvShowViewModel

    @Before
    fun setUp() {
        every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")
        every { appConfigRepository.isTranslationEnabled } returns MutableStateFlow(false)
        every { appConfigRepository.contentLanguage } returns MutableStateFlow("en")
        every { appConfigRepository.preferredOriginalLanguage } returns MutableStateFlow("en")
        every { adConfigProvider.isAdsEnabled } returns false

        every { traktAuthManager.authState } returns traktAuthFlow

        coEvery { trendingTvShowsUseCase(any()) } returns Result.Success(emptyList())
        coEvery { todayAiringTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { nowAiringTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { popularTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { topRatedTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { upcomingTvShowsUseCase() } returns Result.Success(emptyList())
        coEvery { tvGenresUseCase() } returns Result.Success(emptyList())
        coEvery { fetchAllWatchProvidersUseCase.fetchTvWatchProviders() } returns Result.Success(
            emptyList()
        )

        viewModel = HomeTvShowViewModel(
            trendingTvShowsUseCase = trendingTvShowsUseCase,
            topRatedTvShowsUseCase = topRatedTvShowsUseCase,
            upcomingTvShowsUseCase = upcomingTvShowsUseCase,
            todayAiringTvShowsUseCase = todayAiringTvShowsUseCase,
            nowAiringTvShowsUseCase = nowAiringTvShowsUseCase,
            popularTvShowsUseCase = popularTvShowsUseCase,
            tvGenresUseCase = tvGenresUseCase,
            fetchAllWatchProvidersUseCase = fetchAllWatchProvidersUseCase,
            appConfigRepository = appConfigRepository,
            adConfigProvider = adConfigProvider,
            traktAuthManager = traktAuthManager,
            traktSyncRepository = fakeTraktSyncRepository
        )
    }

    @Test
    fun `initial trakt connection state and up next queue are updated reactively`() = runTest {
        advanceUntilIdle()

        traktAuthFlow.value = TraktAuthState.Connected(
            user = mockk(relaxed = true),
            accessToken = "token_abc"
        )
        fakeTraktSyncRepository.upNextQueueFlow.value = listOf(sampleUpNextEpisode)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isTraktConnected).isTrue()
        assertThat(state.upNextQueue).hasSize(1)
        assertThat(state.upNextQueue.first().showTitle).isEqualTo("Severance")
        assertThat(state.upNextQueue.first().totalCompleted).isEqualTo(6)
        assertThat(state.upNextQueue.first().totalAired).isEqualTo(9)
    }

    @Test
    fun `markEpisodeWatched optimistically increments totalCompleted and keeps totalAired unchanged`() =
        runTest {
            traktAuthFlow.value = TraktAuthState.Connected(
                user = mockk(relaxed = true),
                accessToken = "token_abc"
            )
            fakeTraktSyncRepository.upNextQueueFlow.value = listOf(sampleUpNextEpisode)
            advanceUntilIdle()

            viewModel.markEpisodeWatched(
                showTmdbId = 93405,
                season = 2,
                episode = 1
            )

            // Optimistic check: totalCompleted increments to 7, totalAired stays 9, episodeNumber increments to 2
            val state = viewModel.uiState.value
            val updatedItem = state.upNextQueue.first { it.showTmdbId == 93405 }
            assertThat(updatedItem.totalCompleted).isEqualTo(7)
            assertThat(updatedItem.totalAired).isEqualTo(9)
            assertThat(updatedItem.episodeNumber).isEqualTo(2)

            advanceUntilIdle()

            assertThat(fakeTraktSyncRepository.lastMarkedShowTmdbId).isEqualTo(93405)
            assertThat(fakeTraktSyncRepository.lastMarkedSeason).isEqualTo(2)
            assertThat(fakeTraktSyncRepository.lastMarkedEpisode).isEqualTo(1)
            assertThat(fakeTraktSyncRepository.lastMarkedShowTitle).isEqualTo("Severance")
            assertThat(fakeTraktSyncRepository.lastMarkedPosterPath).isEqualTo("poster.jpg")
            assertThat(fakeTraktSyncRepository.lastMarkedTotalAired).isEqualTo(9)
            // Episode title is updated from repository emission
            assertThat(viewModel.uiState.value.upNextQueue.first().episodeTitle).isEqualTo("Episode 2")
        }

    @Test
    fun `markEpisodeWatched on final episode triggers completion dialog and dismiss removes card`() =
        runTest {
            val nearCompleteShow = sampleUpNextEpisode.copy(
                totalCompleted = 8,
                totalAired = 9,
                seasonNumber = 2,
                episodeNumber = 9
            )
            traktAuthFlow.value = TraktAuthState.Connected(
                user = mockk(relaxed = true),
                accessToken = "token_abc"
            )
            fakeTraktSyncRepository.upNextQueueFlow.value = listOf(nearCompleteShow)
            advanceUntilIdle()

            viewModel.markEpisodeWatched(
                showTmdbId = 93405,
                season = 2,
                episode = 9
            )

            // Verify completion dialog is triggered
            val stateWithDialog = viewModel.uiState.value
            assertThat(stateWithDialog.completedShowDialog).isNotNull()
            assertThat(stateWithDialog.completedShowDialog?.showTmdbId).isEqualTo(93405)
            assertThat(stateWithDialog.completedShowDialog?.showTitle).isEqualTo("Severance")
            assertThat(stateWithDialog.completedShowDialog?.totalCompleted).isEqualTo(9)

            // Dismiss dialog
            viewModel.dismissCompletedShowDialog()
            val stateAfterDismiss = viewModel.uiState.value
            assertThat(stateAfterDismiss.completedShowDialog).isNull()
            assertThat(stateAfterDismiss.upNextQueue).isEmpty()
        }

    @Test
    fun `fetchTvGenres success sets uiState with genres`() = runTest {
        val genres = listOf(Genre(id = 18, name = "Drama"))
        coEvery { tvGenresUseCase() } returns Result.Success(genres)

        viewModel.fetchTvGenres()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.genres).isInstanceOf(UiState.Success::class.java)
        val successData = (state.genres as UiState.Success).data
        assertThat(successData).hasSize(1)
        assertThat(successData.first().name).isEqualTo("Drama")
    }

    @Test
    fun `fetchTrendingTvShows success maps and sets trending state`() = runTest {
        val show = mockk<TvShow>(relaxed = true) {
            every { id } returns 101
            every { title } returns "Shogun"
        }
        coEvery { trendingTvShowsUseCase(any()) } returns Result.Success(listOf(show))

        viewModel.fetchTrendingTvShows()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.trendingTvShows).isInstanceOf(UiState.Success::class.java)
    }
}
