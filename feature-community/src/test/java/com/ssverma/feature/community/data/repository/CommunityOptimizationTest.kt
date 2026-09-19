package com.ssverma.feature.community.data.repository

import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CommunityOptimizationTest {

    private val fakeRepo = FakeCommunityRepository()

    @Test
    fun `poll vote status is cached and updates poll model correctly`() = runTest {
        val today = LocalDate.now()

        // 1. Initial state: not voted
        val initialVoted = fakeRepo.isTodayPollVotedFlow(today).first()
        assertFalse(initialVoted)

        val initialPoll = fakeRepo.getDailyPoll(today).first()
        assertNull(initialPoll.selectedOptionIndex)
        assertFalse(initialPoll.hasVoted)

        // 2. Cast a vote
        fakeRepo.voteDailyPoll(date = today, optionIndex = 2)

        // 3. Updated state: voted
        val updatedVoted = fakeRepo.isTodayPollVotedFlow(today).first()
        assertTrue(updatedVoted)

        val updatedPoll = fakeRepo.getDailyPoll(today).first()
        assertTrue(updatedPoll.hasVoted)
    }

    @Test
    fun `community optimization defaults are tuned for Spark plan`() {
        // Trending discussions default limit is 5 (halved from 10)
        assertEquals(5L, CommunityOptimizationConfig.DEFAULT_TRENDING_DISCUSSIONS_LIMIT)

        // Cache TTLs are 60 minutes
        assertEquals(
            3600000L,
            CommunityOptimizationConfig.DEFAULT_TRENDING_DISCUSSIONS_CACHE_TTL_MS
        )
        assertEquals(3600000L, CommunityOptimizationConfig.DEFAULT_DAILY_POLL_CACHE_TTL_MS)
    }
}
