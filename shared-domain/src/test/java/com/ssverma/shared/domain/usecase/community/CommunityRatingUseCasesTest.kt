package com.ssverma.shared.domain.usecase.community

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.domain.repository.CommunityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CommunityRatingUseCasesTest {

    private lateinit var communityRepository: CommunityRepository
    private lateinit var getCommunityRatingUseCase: GetCommunityRatingUseCase
    private lateinit var submitCommunityRatingUseCase: SubmitCommunityRatingUseCase
    private lateinit var removeCommunityRatingUseCase: RemoveCommunityRatingUseCase

    @Before
    fun setUp() {
        communityRepository = mockk(relaxed = true)
        getCommunityRatingUseCase =
            GetCommunityRatingUseCase(communityRepository = communityRepository)
        submitCommunityRatingUseCase =
            SubmitCommunityRatingUseCase(communityRepository = communityRepository)
        removeCommunityRatingUseCase =
            RemoveCommunityRatingUseCase(communityRepository = communityRepository)
    }

    @Test
    fun `GetCommunityRatingUseCase returns flow from repository`() = runTest {
        val expected = CommunityRating.empty(mediaType = MediaType.Movie, mediaId = 550).copy(
            ratingCount = 10,
            averageRating = 4.2f
        )
        every {
            communityRepository.getCommunityRating(mediaType = MediaType.Movie, mediaId = 550)
        } returns flowOf(expected)

        val result = getCommunityRatingUseCase(mediaType = MediaType.Movie, mediaId = 550).first()

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `SubmitCommunityRatingUseCase rejects invalid rating step`() = runTest {
        val result = submitCommunityRatingUseCase(
            mediaType = MediaType.Movie,
            mediaId = 550,
            rating = 4.3f // Invalid increment
        )

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = (result as Result.Error).error
        assertThat(error).isEqualTo(Failure.CoreFailure.UnexpectedFailure)

        coVerify(exactly = 0) {
            communityRepository.submitCommunityRating(any(), any(), any())
        }
    }

    @Test
    fun `SubmitCommunityRatingUseCase delegates valid rating to repository`() = runTest {
        val expected = CommunityRating.empty(mediaType = MediaType.Movie, mediaId = 550).copy(
            ratingCount = 1,
            averageRating = 4.5f,
            userRating = 4.5f
        )
        coEvery {
            communityRepository.submitCommunityRating(
                mediaType = MediaType.Movie,
                mediaId = 550,
                rating = 4.5f
            )
        } returns Result.Success(expected)

        val result = submitCommunityRatingUseCase(
            mediaType = MediaType.Movie,
            mediaId = 550,
            rating = 4.5f
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = (result as Result.Success).data
        assertThat(success).isEqualTo(expected)
        coVerify(exactly = 1) {
            communityRepository.submitCommunityRating(
                mediaType = MediaType.Movie,
                mediaId = 550,
                rating = 4.5f
            )
        }
    }

    @Test
    fun `RemoveCommunityRatingUseCase delegates to repository`() = runTest {
        val expected = CommunityRating.empty(mediaType = MediaType.Movie, mediaId = 550)
        coEvery {
            communityRepository.removeCommunityRating(mediaType = MediaType.Movie, mediaId = 550)
        } returns Result.Success(expected)

        val result = removeCommunityRatingUseCase(
            mediaType = MediaType.Movie,
            mediaId = 550
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify(exactly = 1) {
            communityRepository.removeCommunityRating(mediaType = MediaType.Movie, mediaId = 550)
        }
    }
}
