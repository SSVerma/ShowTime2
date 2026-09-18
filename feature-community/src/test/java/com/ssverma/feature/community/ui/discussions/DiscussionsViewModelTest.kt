package com.ssverma.feature.community.ui.discussions

import android.app.Activity
import android.content.Context
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.analytics.Analytics
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.ThreadFilter
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.FilterAndSortCommentsUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.ValidateCommunityContentUseCase
import com.ssverma.shared.testing.fakes.FakeCommentQuotaManager
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import io.mockk.slot

class DiscussionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeCommunityRepository = FakeCommunityRepository()
    private val fakeCommentQuotaManager = FakeCommentQuotaManager()
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val mockAnalytics: Analytics = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)

    private val target = DiscussionTarget.movie(999)

    private lateinit var getDiscussionsUseCase: GetDiscussionsUseCase
    private lateinit var postCommentUseCase: PostCommentUseCase
    private lateinit var editCommentUseCase: EditCommentUseCase
    private lateinit var reportCommentUseCase: ReportCommentUseCase
    private lateinit var toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase
    private lateinit var deleteCommentUseCase: DeleteCommentUseCase
    private lateinit var filterAndSortCommentsUseCase: FilterAndSortCommentsUseCase

    private lateinit var viewModel: DiscussionsViewModel

    @Before
    fun setUp() {
        getDiscussionsUseCase = GetDiscussionsUseCase(fakeCommunityRepository)
        postCommentUseCase = PostCommentUseCase(
            communityRepository = fakeCommunityRepository,
            commentQuotaManager = fakeCommentQuotaManager
        )
        editCommentUseCase = EditCommentUseCase(fakeCommunityRepository)
        reportCommentUseCase = ReportCommentUseCase(fakeCommunityRepository)
        toggleCommentUpvoteUseCase = ToggleCommentUpvoteUseCase(fakeCommunityRepository)
        deleteCommentUseCase = DeleteCommentUseCase(fakeCommunityRepository)
        filterAndSortCommentsUseCase = FilterAndSortCommentsUseCase()

        viewModel = DiscussionsViewModel(
            getDiscussionsUseCase = getDiscussionsUseCase,
            postCommentUseCase = postCommentUseCase,
            editCommentUseCase = editCommentUseCase,
            reportCommentUseCase = reportCommentUseCase,
            toggleCommentUpvoteUseCase = toggleCommentUpvoteUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            filterAndSortCommentsUseCase = filterAndSortCommentsUseCase,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            analytics = mockAnalytics,
            context = mockContext,
            discussionTarget = target,
            mediaTitle = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg",
            backdropImageUrl = null
        )
    }

    @Test
    fun `postComment when within quota posts successfully`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Consume initial state

            viewModel.postComment("Masterpiece cinema!", isSpoiler = false)

            val state = awaitItem()
            assertThat(state.isQuotaGateVisible).isFalse()
            assertThat(state.comments).hasSize(1)
            assertThat(state.comments[0].content).isEqualTo("Masterpiece cinema!")
            assertThat(state.comments[0].isProUser).isFalse()
        }
    }

    @Test
    fun `postComment when quota exceeded displays quota gate`() = runTest {
        fakeCommentQuotaManager.dailyCommentsCount = 3
        fakeCommentQuotaManager.freeDailyLimit = 3
        fakeCommentQuotaManager.extraCommentSlots = 0

        viewModel.uiState.test {
            awaitItem() // Consume initial state

            viewModel.postComment("Thought beyond quota", isSpoiler = false)

            val state = awaitItem()
            assertThat(state.isQuotaGateVisible).isTrue()
            assertThat(state.comments).isEmpty()
        }
    }

    @Test
    fun `watchAdForCommentPass unlocks quota and automatically submits pending thought`() =
        runTest {
            val mockActivity: Activity = mockk(relaxed = true)
            fakeCommentQuotaManager.dailyCommentsCount = 3
            fakeCommentQuotaManager.freeDailyLimit = 3

            val onRewardSlot = slot<() -> Unit>()
            every {
                mockRewardedAdManager.showRewardedAdIfReady(
                    activity = mockActivity,
                    onAdDismissed = any(),
                    onUserEarnedReward = capture(onRewardSlot)
                )
            } answers {
                onRewardSlot.captured.invoke()
            }

            coEvery { mockRewardManager.grantCommentPass() } coAnswers {
                fakeCommentQuotaManager.grantCommentPass()
            }

            viewModel.uiState.test {
                awaitItem() // Consume initial state

                viewModel.postComment("Thought beyond quota", isSpoiler = false)

                val gateState = awaitItem()
                assertThat(gateState.isQuotaGateVisible).isTrue()

                viewModel.watchAdForCommentPass(mockActivity)

                // isAdLoading = true
                val loadingState = awaitItem()
                assertThat(loadingState.isAdLoading).isTrue()

                // gate dismissed
                val gateDismissedState = awaitItem()
                assertThat(gateDismissedState.isQuotaGateVisible).isFalse()

                // comment posted and list updated
                val successState = awaitItem()
                assertThat(successState.isQuotaGateVisible).isFalse()
                assertThat(successState.comments).hasSize(1)
                assertThat(successState.comments[0].content).isEqualTo("Thought beyond quota")
            }
        }

    @Test
    fun `openPaywall and dismissPaywall update paywall visibility`() = runTest {
        viewModel.openPaywall()
        assertThat(viewModel.uiState.value.isPaywallVisible).isTrue()

        viewModel.dismissPaywall()
        assertThat(viewModel.uiState.value.isPaywallVisible).isFalse()
    }

    @Test
    fun `onFilterSelected updates filter state`() = runTest {
        viewModel.onFilterSelected(ThreadFilter.TOP_UPVOTED)
        assertThat(viewModel.selectedFilter.value).isEqualTo(ThreadFilter.TOP_UPVOTED)
    }
}
