package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IsTodayPollVotedUseCaseTest {

    private val fakeRepo = FakeCommunityRepository()
    private val useCase = IsTodayPollVotedUseCase(communityRepository = fakeRepo)

    @Test
    fun `isTodayPollVoted returns false initially and true after voting`() = runTest {
        val today = LocalDate.now()

        // Initially not voted
        val initialStatus = useCase(today).first()
        assertFalse(initialStatus)

        // Vote in daily poll
        fakeRepo.voteDailyPoll(date = today, optionIndex = 0)

        // Status is updated to true
        val updatedStatus = useCase(today).first()
        assertTrue(updatedStatus)
    }

    @Test
    fun `community optimization config values are verified`() {
        assertEquals(5L, CommunityOptimizationConfig.DEFAULT_TRENDING_DISCUSSIONS_LIMIT)
        assertEquals(
            "remote_trending_discussions_limit",
            CommunityOptimizationConfig.REMOTE_KEY_TRENDING_DISCUSSIONS_LIMIT
        )
        assertEquals(
            60 * 60 * 1000L,
            CommunityOptimizationConfig.DEFAULT_TRENDING_DISCUSSIONS_CACHE_TTL_MS
        )
        assertEquals(
            "remote_trending_discussions_cache_ttl_minutes",
            CommunityOptimizationConfig.REMOTE_KEY_TRENDING_DISCUSSIONS_CACHE_TTL_MINUTES
        )
        assertEquals(
            60 * 60 * 1000L,
            CommunityOptimizationConfig.DEFAULT_DAILY_POLL_CACHE_TTL_MS
        )
        assertEquals(
            "remote_daily_poll_cache_ttl_minutes",
            CommunityOptimizationConfig.REMOTE_KEY_DAILY_POLL_CACHE_TTL_MINUTES
        )
    }
}
