package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.PublishCustomListParams
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.domain.model.community.UnpublishCustomListParams
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CommunityRepository {
    fun getMediaReactions(mediaType: MediaType, mediaId: Int): Flow<MediaReactions>
    suspend fun toggleMediaReaction(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure>

    fun getDailyPoll(date: LocalDate): Flow<DailyPoll>
    suspend fun voteDailyPoll(
        date: LocalDate,
        optionIndex: Int
    ): Result<DailyPoll, Failure.CoreFailure>

    fun getDiscussions(target: DiscussionTarget): Flow<List<Comment>>

    suspend fun postComment(params: PostCommentParams): Result<Comment, Failure.CoreFailure>

    suspend fun editComment(params: EditCommentParams): Result<Unit, Failure.CoreFailure>

    suspend fun reportComment(params: ReportCommentParams): Result<Unit, Failure.CoreFailure>

    suspend fun toggleCommentUpvote(params: ToggleCommentUpvoteParams): Result<Unit, Failure.CoreFailure>

    suspend fun deleteComment(params: DeleteCommentParams): Result<Unit, Failure.CoreFailure>

    fun getTrendingDiscussions(): Flow<List<TrendingDiscussion>>

    fun getCommunityCuratedLists(category: String? = null): Flow<List<CommunityCuratedList>>

    fun getCommunityListDetails(listId: String): Flow<CommunityCuratedList?>

    suspend fun publishCustomList(params: PublishCustomListParams): Result<Unit, Failure.CoreFailure>

    suspend fun unpublishCustomList(params: UnpublishCustomListParams): Result<Unit, Failure.CoreFailure>

    suspend fun toggleCommunityListUpvote(params: ToggleListUpvoteParams): Result<Unit, Failure.CoreFailure>

    suspend fun recordListClone(listId: String): Result<Unit, Failure.CoreFailure>
}
