package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.repository.CommunityRepository
import com.ssverma.shared.domain.usecase.community.GetMediaReactionsUseCase
import com.ssverma.shared.domain.usecase.community.ToggleMediaReactionUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CommunityReactionsTest {

    @Test
    fun `MediaReactions empty returns zero totals and empty tags`() {
        val empty = MediaReactions.empty(mediaType = MediaType.Movie, mediaId = 123)

        assertThat(empty.totalReactions).isEqualTo(0)
        assertThat(empty.tagCounts).isEmpty()
        assertThat(empty.userSelectedTags).isEmpty()
        assertThat(empty.getPercentageForTag(MediaReactionTag.MIND_BENDING)).isWithin(0.01f).of(0f)
        assertThat(empty.isTagSelected(MediaReactionTag.MIND_BENDING)).isFalse()
    }

    @Test
    fun `MediaReactions correctly computes percentages and counts`() {
        val reactions = MediaReactions(
            mediaType = MediaType.Movie,
            mediaId = 550,
            tagCounts = mapOf(
                MediaReactionTag.MIND_BENDING to 60,
                MediaReactionTag.PLOT_TWIST to 30,
                MediaReactionTag.COMFORT_WATCH to 10
            ),
            totalReactions = 100,
            userSelectedTags = setOf(MediaReactionTag.MIND_BENDING, MediaReactionTag.PLOT_TWIST)
        )

        assertThat(reactions.getPercentageForTag(MediaReactionTag.MIND_BENDING)).isWithin(0.01f)
            .of(60f)
        assertThat(reactions.getPercentageForTag(MediaReactionTag.PLOT_TWIST)).isWithin(0.01f)
            .of(30f)
        assertThat(reactions.getPercentageForTag(MediaReactionTag.COMFORT_WATCH)).isWithin(0.01f)
            .of(10f)
        assertThat(reactions.getPercentageForTag(MediaReactionTag.OVERRATED)).isWithin(0.01f).of(0f)

        assertThat(reactions.isTagSelected(MediaReactionTag.MIND_BENDING)).isTrue()
        assertThat(reactions.isTagSelected(MediaReactionTag.PLOT_TWIST)).isTrue()
        assertThat(reactions.isTagSelected(MediaReactionTag.COMFORT_WATCH)).isFalse()
    }

    @Test
    fun `MediaReactionTag fromTagKey parses valid keys`() {
        assertThat(MediaReactionTag.fromTagKey("mind_bending")).isEqualTo(MediaReactionTag.MIND_BENDING)
        assertThat(MediaReactionTag.fromTagKey("comfort_watch")).isEqualTo(MediaReactionTag.COMFORT_WATCH)
        assertThat(MediaReactionTag.fromTagKey("plot_twist")).isEqualTo(MediaReactionTag.PLOT_TWIST)
        assertThat(MediaReactionTag.fromTagKey("imax_essential")).isEqualTo(MediaReactionTag.IMAX_ESSENTIAL)
        assertThat(MediaReactionTag.fromTagKey("cried_eyes_out")).isEqualTo(MediaReactionTag.EMOTIONAL_TEARJERKER)
        assertThat(MediaReactionTag.fromTagKey("overrated")).isEqualTo(MediaReactionTag.OVERRATED)
        assertThat(MediaReactionTag.fromTagKey("non_existent")).isNull()
    }

    @Test
    fun `GetMediaReactionsUseCase delegates to CommunityRepository`() = runTest {
        val mockRepo = mockk<CommunityRepository>()
        val expected = MediaReactions(
            mediaType = MediaType.Tv,
            mediaId = 999,
            totalReactions = 42
        )
        every { mockRepo.getMediaReactions(MediaType.Tv, 999) } returns flowOf(expected)

        val useCase = GetMediaReactionsUseCase(communityRepository = mockRepo)
        val result = useCase(mediaType = MediaType.Tv, mediaId = 999).first()

        assertThat(result.totalReactions).isEqualTo(42)
    }

    @Test
    fun `ToggleMediaReactionUseCase invokes toggle on repository`() = runTest {
        val mockRepo = mockk<CommunityRepository>()
        val updated = MediaReactions(
            mediaType = MediaType.Movie,
            mediaId = 100,
            userSelectedTags = setOf(MediaReactionTag.IMAX_ESSENTIAL),
            totalReactions = 1
        )
        coEvery {
            mockRepo.toggleMediaReaction(MediaType.Movie, 100, MediaReactionTag.IMAX_ESSENTIAL)
        } returns Result.Success(updated)

        val useCase = ToggleMediaReactionUseCase(communityRepository = mockRepo)
        val result = useCase(
            mediaType = MediaType.Movie,
            mediaId = 100,
            tag = MediaReactionTag.IMAX_ESSENTIAL
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.data.isTagSelected(MediaReactionTag.IMAX_ESSENTIAL)).isTrue()
    }
}
