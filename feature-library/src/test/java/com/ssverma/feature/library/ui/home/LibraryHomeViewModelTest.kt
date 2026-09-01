package com.ssverma.feature.library.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListDetailsUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.PublishCustomListUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.UnpublishCustomListUseCase
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LibraryHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeCommunityRepository: FakeCommunityRepository
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())

    private lateinit var viewModel: LibraryHomeViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeCommunityRepository = FakeCommunityRepository()
        every { mockRewardManager.passStatus } returns passStatusFlow
        coEvery { mockRewardManager.canCreateCustomList(any(), any()) } returns true

        viewModel = LibraryHomeViewModel(
            libraryRepository = fakeLibraryRepository,
            getCommunityListsUseCase = GetCommunityListsUseCase(
                communityRepository = fakeCommunityRepository
            ),
            getCommunityListDetailsUseCase = GetCommunityListDetailsUseCase(
                communityRepository = fakeCommunityRepository
            ),
            publishCustomListUseCase = PublishCustomListUseCase(
                communityRepository = fakeCommunityRepository,
                libraryRepository = fakeLibraryRepository
            ),
            unpublishCustomListUseCase = UnpublishCustomListUseCase(
                communityRepository = fakeCommunityRepository,
                libraryRepository = fakeLibraryRepository
            ),
            toggleCommunityListUpvoteUseCase = ToggleCommunityListUpvoteUseCase(
                communityRepository = fakeCommunityRepository
            ),
            cloneCommunityListUseCase = CloneCommunityListUseCase(
                libraryRepository = fakeLibraryRepository,
                communityRepository = fakeCommunityRepository
            ),
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `onAttemptCreateList opens create list dialog when under quota`() = runTest {
        coEvery { mockRewardManager.canCreateCustomList(0, false) } returns true

        viewModel.onAttemptCreateList()

        assertThat(viewModel.isCreateListDialogVisible.value).isTrue()
        assertThat(viewModel.isQuotaGateVisible.value).isFalse()
    }

    @Test
    fun `onAttemptCreateList triggers quota gate when limit reached for free user`() = runTest {
        coEvery { mockRewardManager.canCreateCustomList(any(), false) } returns false

        viewModel.onAttemptCreateList()

        assertThat(viewModel.isQuotaGateVisible.value).isTrue()
        assertThat(viewModel.isCreateListDialogVisible.value).isFalse()
    }

    @Test
    fun `dismissQuotaGate closes bottom sheet`() = runTest {
        coEvery { mockRewardManager.canCreateCustomList(any(), false) } returns false
        viewModel.onAttemptCreateList()
        assertThat(viewModel.isQuotaGateVisible.value).isTrue()

        viewModel.dismissQuotaGate()
        assertThat(viewModel.isQuotaGateVisible.value).isFalse()
    }

    @Test
    fun `dismissCreateListDialog closes create dialog`() = runTest {
        viewModel.onAttemptCreateList()
        assertThat(viewModel.isCreateListDialogVisible.value).isTrue()

        viewModel.dismissCreateListDialog()
        assertThat(viewModel.isCreateListDialogVisible.value).isFalse()
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
    fun `favoriteItems reflects added and removed favorites`() = runTest {
        viewModel.favoriteItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleFavorite(
                mediaId = 202,
                mediaType = MediaType.Tv,
                title = "Dark",
                posterImageUrl = "/dark.jpg",
                backdropImageUrl = "/dark_back.jpg",
                voteAvg = 9.0f,
                releaseDate = "2017-12-01"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Dark")

            viewModel.removeFromFavorites(202)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `historyItems reflects added history and clears history`() = runTest {
        viewModel.historyItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleWatchHistory(
                mediaId = 303,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                voteAvg = 8.6f
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Interstellar")

            viewModel.clearHistory()
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `customLists reflects created, updated, and deleted lists`() = runTest {
        viewModel.customLists.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.createCustomList("Cyberpunk Essentials", "Top neon cinema")

            val created = awaitItem()
            assertThat(created).hasSize(1)
            val list = created.first()
            assertThat(list.title).isEqualTo("Cyberpunk Essentials")

            viewModel.updateCustomList(list.listId, "Neo Tokyo", "Updated description")
            val updated = awaitItem()
            assertThat(updated.first().title).isEqualTo("Neo Tokyo")

            viewModel.deleteCustomList(list.listId)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `filters update state flow correctly`() = runTest {
        viewModel.setWatchlistFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.MOVIE)

        viewModel.setFavoritesFilter(MediaTypeFilter.TV)
        assertThat(viewModel.favoritesFilter.value).isEqualTo(MediaTypeFilter.TV)

        viewModel.setHistoryFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.historyFilter.value).isEqualTo(MediaTypeFilter.MOVIE)
    }
}
