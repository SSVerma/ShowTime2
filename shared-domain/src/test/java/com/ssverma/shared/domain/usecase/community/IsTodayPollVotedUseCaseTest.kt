package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import com.ssverma.shared.domain.repository.CommunityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class IsTodayPollVotedUseCaseTest {

    private val communityRepository: CommunityRepository = mockk()
    private val useCase = IsTodayPollVotedUseCase(communityRepository = communityRepository)

    @Test
    fun `isTodayPollVoted returns false initially and true after voting`() = runTest {
        val today = LocalDate.now()

        // Initially not voted
        every { communityRepository.isTodayPollVotedFlow(date = today) } returns flowOf(false)
        val initialStatus = useCase(today).first()
        assertFalse(initialStatus)

        // Status is updated to true
        every { communityRepository.isTodayPollVotedFlow(date = today) } returns flowOf(true)
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
