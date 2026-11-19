package com.ssverma.feature.community.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.repository.CommunityRepository
import com.ssverma.shared.domain.usecase.community.GetCommunityRatingUseCase
import com.ssverma.shared.domain.usecase.community.RemoveCommunityRatingUseCase
import com.ssverma.shared.domain.usecase.community.SubmitCommunityRatingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CommunityRatingsTest {

    @Test
    fun `GetCommunityRatingUseCase retrieves rating stream from repository`() = runTest {
        val repo: CommunityRepository = mockk()
        val expected = CommunityRating(
            mediaType = MediaType.Movie,
            mediaId = 550,
            ratingCount = 42,
            averageRating = 4.3f,
            userRating = 4.5f
        )
        every { repo.getCommunityRating(MediaType.Movie, 550) } returns flowOf(expected)

        val useCase = GetCommunityRatingUseCase(communityRepository = repo)
        val result = useCase(MediaType.Movie, 550).first()

        assertThat(result).isEqualTo(expected)
        assertThat(result.hasSufficientRatings(threshold = 5)).isTrue()
    }

    @Test
    fun `SubmitCommunityRatingUseCase validates and delegates valid rating`() = runTest {
        val repo: CommunityRepository = mockk()
        val expected = CommunityRating(
            mediaType = MediaType.Tv,
            mediaId = 1399,
            ratingCount = 100,
            averageRating = 4.8f,
            userRating = 5.0f
        )
        coEvery { repo.submitCommunityRating(MediaType.Tv, 1399, 5.0f) } returns Result.Success(
            expected
        )

        val useCase = SubmitCommunityRatingUseCase(communityRepository = repo)
        val result = useCase(MediaType.Tv, 1399, 5.0f)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(expected)
        coVerify(exactly = 1) { repo.submitCommunityRating(MediaType.Tv, 1399, 5.0f) }
    }

    @Test
    fun `RemoveCommunityRatingUseCase removes rating and clears userRating`() = runTest {
        val repo: CommunityRepository = mockk()
        val expected = CommunityRating.empty(MediaType.Movie, 550)
        coEvery {
            repo.removeCommunityRating(
                MediaType.Movie,
                550
            )
        } returns Result.Success(expected)

        val useCase = RemoveCommunityRatingUseCase(communityRepository = repo)
        val result = useCase(MediaType.Movie, 550)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data.userRating).isNull()
        coVerify(exactly = 1) { repo.removeCommunityRating(MediaType.Movie, 550) }
    }
}
