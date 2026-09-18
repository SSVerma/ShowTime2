package com.ssverma.shared.domain.usecase.community

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.repository.CommentQuotaManager
import com.ssverma.shared.domain.repository.CommunityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PostCommentUseCaseTest {

    private val communityRepository: CommunityRepository = mockk(relaxed = true)
    private val commentQuotaManager: CommentQuotaManager = mockk(relaxed = true)
    private val validateCommunityContentUseCase = ValidateCommunityContentUseCase()

    private lateinit var useCase: PostCommentUseCase

    private val sampleTarget = DiscussionTarget.movie(123)

    @Before
    fun setUp() {
        useCase = PostCommentUseCase(
            communityRepository = communityRepository,
            commentQuotaManager = commentQuotaManager,
            validateCommunityContentUseCase = validateCommunityContentUseCase
        )
    }

    @Test
    fun `invoke returns QuotaExceeded when quota manager blocks comment`() = runTest {
        coEvery { commentQuotaManager.canPostComment(isProActive = false) } returns false

        val params = PostCommentParams(
            target = sampleTarget,
            content = "This movie is awesome!",
            isSpoiler = false,
            isProUser = false
        )

        val result = useCase(params)

        assertThat(result).isInstanceOf(PostCommentResult.QuotaExceeded::class.java)
        coVerify(exactly = 0) { communityRepository.postComment(any()) }
        coVerify(exactly = 0) { commentQuotaManager.recordCommentPosted() }
    }

    @Test
    fun `invoke returns Error when validation fails`() = runTest {
        coEvery { commentQuotaManager.canPostComment(any()) } returns true

        val params = PostCommentParams(
            target = sampleTarget,
            content = "   ",
            isSpoiler = false
        )

        val result = useCase(params)

        assertThat(result).isInstanceOf(PostCommentResult.Error::class.java)
        coVerify(exactly = 0) { communityRepository.postComment(any()) }
        coVerify(exactly = 0) { commentQuotaManager.recordCommentPosted() }
    }

    @Test
    fun `invoke posts comment and records quota when allowed`() = runTest {
        val expectedComment = Comment(
            id = "c_1",
            authorId = "user_1",
            authorName = "MovieFan",
            content = "Great cinema!",
            isSpoiler = false,
            isProUser = false
        )

        coEvery { commentQuotaManager.canPostComment(isProActive = false) } returns true
        coEvery { communityRepository.postComment(any()) } returns Result.Success(expectedComment)

        val params = PostCommentParams(
            target = sampleTarget,
            content = "Great cinema!",
            isSpoiler = false,
            isProUser = false
        )

        val result = useCase(params)

        assertThat(result).isInstanceOf(PostCommentResult.Success::class.java)
        assertThat((result as PostCommentResult.Success).comment).isEqualTo(expectedComment)
        coVerify(exactly = 1) { communityRepository.postComment(params) }
        coVerify(exactly = 1) { commentQuotaManager.recordCommentPosted() }
    }

    @Test
    fun `invoke propagates isProUser flag to repository`() = runTest {
        val expectedProComment = Comment(
            id = "c_pro",
            authorId = "user_pro",
            authorName = "ProCritic",
            content = "Exceptional cinematography.",
            isSpoiler = false,
            isProUser = true
        )

        coEvery { commentQuotaManager.canPostComment(isProActive = true) } returns true
        coEvery { communityRepository.postComment(any()) } returns Result.Success(expectedProComment)

        val params = PostCommentParams(
            target = sampleTarget,
            content = "Exceptional cinematography.",
            isSpoiler = false,
            isProUser = true
        )

        val result = useCase(params)

        assertThat(result).isInstanceOf(PostCommentResult.Success::class.java)
        assertThat((result as PostCommentResult.Success).comment.isProUser).isTrue()
        coVerify(exactly = 1) {
            communityRepository.postComment(match { it.isProUser })
        }
        coVerify(exactly = 1) { commentQuotaManager.recordCommentPosted() }
    }
}
