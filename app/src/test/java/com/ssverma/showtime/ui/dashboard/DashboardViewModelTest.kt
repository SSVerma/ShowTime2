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
import com.ssverma.shared.domain.model.community.CloneCommunityListParams
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.GetDailyPollUseCase
import com.ssverma.shared.domain.usecase.community.GetTrendingDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.IsTodayPollVotedUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.VoteDailyPollUseCase
import com.ssverma.shared.domain.usecase.library.GetCustomListsUseCase
import com.ssverma.shared.testing.fakes.FakeTraktSyncRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
    private val isTodayPollVotedUseCase: IsTodayPollVotedUseCase = mockk(relaxed = true)
    private val voteDailyPollUseCase: VoteDailyPollUseCase = mockk(relaxed = true)
    private val getTrendingDiscussionsUseCase: GetTrendingDiscussionsUseCase = mockk(relaxed = true)
    private val getCommunityListsUseCase: GetCommunityListsUseCase = mockk(relaxed = true)
    private val getCustomListsUseCase: GetCustomListsUseCase = mockk(relaxed = true)
    private val toggleCommunityListUpvoteUseCase: ToggleCommunityListUpvoteUseCase =
        mockk(relaxed = true)
    private val cloneCommunityListUseCase: CloneCommunityListUseCase = mockk(relaxed = true)
    private val movieGenresUseCase: MovieGenresUseCase = mockk(relaxed = true)
    private val tvGenresUseCase: TvGenresUseCase = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)
    private val billingRepository: BillingRepository = mockk(relaxed = true)

    private val isProActiveFlow = MutableStateFlow(false)
    private val isTodayPollVotedFlow = MutableStateFlow(false)
    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)
    private val notificationShelfDismissedFlow = MutableStateFlow(0L)
    private val acknowledgedFeaturesFlow = MutableStateFlow<Set<String>>(emptySet())
    private val lastSeenWhatsNewCampaignFlow = MutableStateFlow("")
    private val whatsNewCampaignIdFlow = MutableStateFlow("2.0.0")
    private val isWhatsNewEnabledFlow = MutableStateFlow(true)

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
        every { appConfigRepository.acknowledgedFeatures } returns acknowledgedFeaturesFlow
        coEvery { appConfigRepository.acknowledgeFeature(any()) } coAnswers {
            acknowledgedFeaturesFlow.value = acknowledgedFeaturesFlow.value + firstArg<String>()
        }
        every { appConfigRepository.lastSeenWhatsNewCampaign } returns lastSeenWhatsNewCampaignFlow
        every { appConfigRepository.whatsNewCampaignId } returns whatsNewCampaignIdFlow
        every { appConfigRepository.isWhatsNewEnabled } returns isWhatsNewEnabledFlow
        coEvery { appConfigRepository.updateLastSeenWhatsNewCampaign(any()) } coAnswers {
            lastSeenWhatsNewCampaignFlow.value = firstArg<String>()
        }
        every { adConfigProvider.isAdsEnabled } returns false

        every { traktAuthManager.authState } returns traktAuthFlow
        every { isTodayPollVotedUseCase(any()) } returns isTodayPollVotedFlow
        every { getDailyPollUseCase(any(), any()) } returns MutableStateFlow(
            DailyPoll.empty(LocalDate.now())
        )
        every { getTrendingDiscussionsUseCase(any()) } returns MutableStateFlow(emptyList())
        every { getCommunityListsUseCase(any(), any()) } returns MutableStateFlow(emptyList())
        every { getCustomListsUseCase() } returns MutableStateFlow(emptyList())

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
            isTodayPollVotedUseCase = isTodayPollVotedUseCase,
            voteDailyPollUseCase = voteDailyPollUseCase,
            getTrendingDiscussionsUseCase = getTrendingDiscussionsUseCase,
            getCommunityListsUseCase = getCommunityListsUseCase,
            getCustomListsUseCase = getCustomListsUseCase,
            toggleCommunityListUpvoteUseCase = toggleCommunityListUpvoteUseCase,
            cloneCommunityListUseCase = cloneCommunityListUseCase,
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
    fun `openDailyPollSheet and dismissDailyPollSheet toggle sheet visibility and trigger on demand poll fetch`() =
        runTest {
            assertThat(viewModel.uiState.value.showDailyPollSheet).isFalse()

            val samplePoll = DailyPoll(
                dateString = "2026-09-17",
                questionId = 42,
                question = "Which film is Nolan's masterpiece?",
                options = listOf("Inception", "Interstellar", "Oppenheimer"),
                voteCounts = listOf(10, 20, 30),
                totalVotes = 60,
                selectedOptionIndex = null,
                isEnabled = true
            )
            val pollFlow = MutableStateFlow(samplePoll)
            every { getDailyPollUseCase(any(), any()) } returns pollFlow

            viewModel.openDailyPollSheet()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showDailyPollSheet).isTrue()
            assertThat(viewModel.uiState.value.dailyPoll.questionId).isEqualTo(42)

            viewModel.dismissDailyPollSheet()
            assertThat(viewModel.uiState.value.showDailyPollSheet).isFalse()
        }

    @Test
    fun `isTodayPollVotedFlow updates isTodayPollVoted ui state`() = runTest {
        assertThat(viewModel.uiState.value.isTodayPollVoted).isFalse()

        isTodayPollVotedFlow.value = true
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isTodayPollVoted).isTrue()
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

    @Test
    fun `acknowledgedFeatures updates uiState when repository emits`() = runTest {
        assertThat(viewModel.uiState.value.acknowledgedFeatures).isEmpty()

        acknowledgedFeaturesFlow.value = setOf(CinephileFeature.CINEMA_DIARY.id)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.acknowledgedFeatures)
            .contains(CinephileFeature.CINEMA_DIARY.id)
    }

    @Test
    fun `onFeatureTapped acknowledges feature in repository`() = runTest {
        viewModel.onFeatureTapped(CinephileFeature.MOVIE_MATCH)
        advanceUntilIdle()

        coVerify { appConfigRepository.acknowledgeFeature(CinephileFeature.MOVIE_MATCH.id) }
        assertThat(viewModel.uiState.value.acknowledgedFeatures)
            .contains(CinephileFeature.MOVIE_MATCH.id)
    }

    @Test
    fun `setCuratedCommunitySelected updates isCuratedCommunitySelected in state`() {
        assertThat(viewModel.uiState.value.isCuratedCommunitySelected).isFalse()

        viewModel.setCuratedCommunitySelected(true)
        assertThat(viewModel.uiState.value.isCuratedCommunitySelected).isTrue()

        viewModel.setCuratedCommunitySelected(false)
        assertThat(viewModel.uiState.value.isCuratedCommunitySelected).isFalse()
    }

    @Test
    fun `community lists are loaded on demand and not re-queried on subsequent toggles`() =
        runTest {
            // On initialization, community lists are not loaded
            verify(exactly = 0) { getCommunityListsUseCase(any(), any()) }

            // First toggle to community loads community lists
            viewModel.setCuratedCommunitySelected(true)
            advanceUntilIdle()
            verify(exactly = 1) { getCommunityListsUseCase(any(), any()) }

            // Switching back to my lists does not query again
            viewModel.setCuratedCommunitySelected(false)
            advanceUntilIdle()
            verify(exactly = 1) { getCommunityListsUseCase(any(), any()) }

            // Switching to community again does not re-query
            viewModel.setCuratedCommunitySelected(true)
            advanceUntilIdle()
            verify(exactly = 1) { getCommunityListsUseCase(any(), any()) }
        }

    @Test
    fun `selectCommunityListForDetail updates selectedCommunityListForDetail in state`() {
        val testList = CommunityCuratedList(
            listId = "list_123",
            title = "Mind-Bending Sci-Fi",
            description = "Great sci-fi movies",
            categoryTag = "Sci-Fi",
            authorId = "user_1",
            authorName = "Alex",
            authorAvatarUrl = null,
            itemCount = 10,
            previewPosters = emptyList(),
            upvotesCount = 42,
            clonesCount = 5,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = false,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 2000L,
            items = emptyList()
        )

        viewModel.selectCommunityListForDetail(testList)
        assertThat(viewModel.uiState.value.selectedCommunityListForDetail).isEqualTo(testList)

        viewModel.selectCommunityListForDetail(null)
        assertThat(viewModel.uiState.value.selectedCommunityListForDetail).isNull()
    }

    @Test
    fun `toggleCommunityListUpvote invokes toggleCommunityListUpvoteUseCase`() = runTest {
        viewModel.toggleCommunityListUpvote("list_123")
        advanceUntilIdle()

        coVerify { toggleCommunityListUpvoteUseCase(ToggleListUpvoteParams("list_123")) }
    }

    @Test
    fun `cloneCommunityList invokes cloneCommunityListUseCase and triggers onSuccess callback`() =
        runTest {
            val testList = CommunityCuratedList(
                listId = "list_123",
                title = "Mind-Bending Sci-Fi",
                description = "Great sci-fi movies",
                categoryTag = "Sci-Fi",
                authorId = "user_1",
                authorName = "Alex",
                authorAvatarUrl = null,
                itemCount = 10,
                previewPosters = emptyList(),
                upvotesCount = 42,
                clonesCount = 5,
                isUpvotedByMe = false,
                isClonedByMe = false,
                isMine = false,
                createdAtEpochMs = 1000L,
                updatedAtEpochMs = 2000L,
                items = emptyList()
            )

            coEvery { cloneCommunityListUseCase(any()) } returns Result.Success("local_123")

            var successCalled = false
            var errorCalled = false

            viewModel.cloneCommunityList(
                communityList = testList,
                onSuccess = { successCalled = true },
                onError = { errorCalled = true }
            )
            advanceUntilIdle()

            assertThat(successCalled).isTrue()
            assertThat(errorCalled).isFalse()
            coVerify { cloneCommunityListUseCase(CloneCommunityListParams(testList)) }
        }

    @Test
    fun `retryDailyPoll resets loading and updates poll state`() = runTest {
        val samplePoll = DailyPoll(
            dateString = "2026-09-17",
            questionId = 42,
            question = "Which film is Nolan's masterpiece?",
            options = listOf("Inception", "Interstellar", "Oppenheimer"),
            voteCounts = listOf(10, 20, 30),
            totalVotes = 60,
            selectedOptionIndex = null,
            isEnabled = true
        )
        val pollFlow = MutableStateFlow(DailyPoll.empty(LocalDate.now()))
        every { getDailyPollUseCase(any(), any()) } returns pollFlow

        viewModel.retryDailyPoll()
        pollFlow.value = samplePoll
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isDailyPollLoading).isFalse()
        assertThat(state.dailyPoll.questionId).isEqualTo(42)
        assertThat(state.dailyPoll.question).isEqualTo("Which film is Nolan's masterpiece?")
        assertThat(state.dailyPollError).isNull()
    }

    @Test
    fun `voteDailyPoll updates poll state optimistically before network completes`() = runTest {
        val initialPoll = DailyPoll(
            dateString = "2026-09-17",
            questionId = 42,
            question = "Which film is Nolan's masterpiece?",
            options = listOf("Inception", "Interstellar", "Oppenheimer"),
            voteCounts = listOf(10, 20, 30),
            totalVotes = 60,
            selectedOptionIndex = null,
            isEnabled = true
        )
        every {
            getDailyPollUseCase(
                date = any(),
                forceRefresh = any()
            )
        } returns flowOf(initialPoll)
        viewModel.openDailyPollSheet()

        val stateBefore = viewModel.uiState.value
        assertThat(stateBefore.dailyPoll.selectedOptionIndex).isNull()

        // Call voteDailyPoll without advanceUntilIdle()
        viewModel.voteDailyPoll(optionIndex = 1)

        // Optimistic UI state must be updated immediately (0ms)
        val optimisticState = viewModel.uiState.value
        assertThat(optimisticState.dailyPoll.selectedOptionIndex).isEqualTo(1)
        assertThat(optimisticState.dailyPoll.totalVotes).isEqualTo(61)
        assertThat(optimisticState.dailyPoll.getVoteCount(1)).isEqualTo(21)
        assertThat(optimisticState.dailyPoll.hasVoted).isTrue()
        assertThat(optimisticState.isTodayPollVoted).isTrue()
    }

    @Test
    fun `voteDailyPoll updates poll state on success`() = runTest {
        val votedPoll = DailyPoll(
            dateString = "2026-09-17",
            questionId = 42,
            question = "Which film is Nolan's masterpiece?",
            options = listOf("Inception", "Interstellar", "Oppenheimer"),
            voteCounts = listOf(10, 21, 30),
            totalVotes = 61,
            selectedOptionIndex = 1,
            isEnabled = true
        )
        coEvery { voteDailyPollUseCase(date = any(), optionIndex = 1) } returns Result.Success(
            votedPoll
        )

        viewModel.voteDailyPoll(optionIndex = 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.dailyPoll.selectedOptionIndex).isEqualTo(1)
        assertThat(state.dailyPoll.totalVotes).isEqualTo(61)
        assertThat(state.dailyPoll.hasVoted).isTrue()
    }

    @Test
    fun `whats new banner is visible when user has not seen current campaign and whats new is enabled`() =
        runTest {
            lastSeenWhatsNewCampaignFlow.value = ""
            whatsNewCampaignIdFlow.value = "2.0.0"
            isWhatsNewEnabledFlow.value = true
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.showWhatsNewBanner).isTrue()
            assertThat(state.currentCampaignId).isEqualTo("2.0.0")
        }

    @Test
    fun `whats new banner is hidden when last seen campaign matches current campaign`() =
        runTest {
            lastSeenWhatsNewCampaignFlow.value = "2.0.0"
            whatsNewCampaignIdFlow.value = "2.0.0"
            isWhatsNewEnabledFlow.value = true
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.showWhatsNewBanner).isFalse()
        }

    @Test
    fun `dismissWhatsNewBanner updates last seen campaign in repository`() = runTest {
        lastSeenWhatsNewCampaignFlow.value = ""
        whatsNewCampaignIdFlow.value = "2.0.0"
        isWhatsNewEnabledFlow.value = true
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showWhatsNewBanner).isTrue()

        viewModel.dismissWhatsNewBanner()
        advanceUntilIdle()

        coVerify { appConfigRepository.updateLastSeenWhatsNewCampaign("2.0.0") }
        assertThat(lastSeenWhatsNewCampaignFlow.value).isEqualTo("2.0.0")
        assertThat(viewModel.uiState.value.showWhatsNewBanner).isFalse()
    }
}

