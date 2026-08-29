package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityCuratedListItem
import com.ssverma.shared.domain.model.community.CommunityListCategories
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
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeCommunityRepository : CommunityRepository {

    private val communityLists = MutableStateFlow<List<CommunityCuratedList>>(emptyList())
    private val discussions = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    private val reactions = MutableStateFlow<Map<String, MediaReactions>>(emptyMap())

    override fun getMediaReactions(mediaType: MediaType, mediaId: Int): Flow<MediaReactions> {
        val key = "${mediaType}_$mediaId"
        return reactions.map { it[key] ?: MediaReactions.empty(mediaType, mediaId) }
    }

    override suspend fun toggleMediaReaction(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure> {
        val key = "${mediaType}_$mediaId"
        val current = reactions.value[key] ?: MediaReactions.empty(mediaType, mediaId)
        val isSelected = current.userSelectedTags.contains(tag)
        val updatedTags =
            if (isSelected) current.userSelectedTags - tag else current.userSelectedTags + tag
        val updatedTagCounts = current.tagCounts.toMutableMap()
        val currentCount = updatedTagCounts[tag] ?: 0
        updatedTagCounts[tag] = (currentCount + if (isSelected) -1 else 1).coerceAtLeast(0)
        val updated = current.copy(
            totalReactions = (current.totalReactions + if (isSelected) -1 else 1).coerceAtLeast(0),
            tagCounts = updatedTagCounts,
            userSelectedTags = updatedTags
        )
        reactions.value = reactions.value + (key to updated)
        return Result.Success(updated)
    }

    override fun getDailyPoll(date: LocalDate): Flow<DailyPoll> {
        return MutableStateFlow(DailyPoll.empty(date))
    }

    override suspend fun voteDailyPoll(
        date: LocalDate,
        optionIndex: Int
    ): Result<DailyPoll, Failure.CoreFailure> {
        return Result.Success(DailyPoll.empty(date).copy(selectedOptionIndex = optionIndex))
    }

    override fun getDiscussions(target: DiscussionTarget): Flow<List<Comment>> {
        val key = "${target.mediaType}_${target.mediaId}"
        return discussions.map { it[key] ?: emptyList() }
    }

    override suspend fun postComment(params: PostCommentParams): Result<Comment, Failure.CoreFailure> {
        val key = "${params.target.mediaType}_${params.target.mediaId}"
        val newComment = Comment(
            id = "c_${System.currentTimeMillis()}",
            authorId = "fake_user",
            authorName = "Fake User",
            content = params.content,
            isSpoiler = params.isSpoiler
        )
        val currentList = discussions.value[key] ?: emptyList()
        discussions.value = discussions.value + (key to (currentList + newComment))
        return Result.Success(newComment)
    }

    override suspend fun editComment(params: EditCommentParams): Result<Unit, Failure.CoreFailure> {
        val key = "${params.target.mediaType}_${params.target.mediaId}"
        val currentList = discussions.value[key] ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == params.commentId) it.copy(
                content = params.newContent,
                isSpoiler = params.isSpoiler,
                isEdited = true
            )
            else it
        }
        discussions.value = discussions.value + (key to updatedList)
        return Result.Success(Unit)
    }

    override suspend fun reportComment(params: ReportCommentParams): Result<Unit, Failure.CoreFailure> {
        return Result.Success(Unit)
    }

    override suspend fun toggleCommentUpvote(params: ToggleCommentUpvoteParams): Result<Unit, Failure.CoreFailure> {
        val key = "${params.target.mediaType}_${params.target.mediaId}"
        val currentList = discussions.value[key] ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == params.commentId) {
                val newUpvoted = !it.isUpvotedByMe
                it.copy(
                    isUpvotedByMe = newUpvoted,
                    upvotesCount = it.upvotesCount + if (newUpvoted) 1 else -1
                )
            } else it
        }
        discussions.value = discussions.value + (key to updatedList)
        return Result.Success(Unit)
    }

    override suspend fun deleteComment(params: DeleteCommentParams): Result<Unit, Failure.CoreFailure> {
        val key = "${params.target.mediaType}_${params.target.mediaId}"
        val currentList = discussions.value[key] ?: emptyList()
        discussions.value =
            discussions.value + (key to currentList.filterNot { it.id == params.commentId })
        return Result.Success(Unit)
    }

    override fun getTrendingDiscussions(): Flow<List<TrendingDiscussion>> {
        return MutableStateFlow(emptyList())
    }

    override fun getCommunityCuratedLists(category: String?): Flow<List<CommunityCuratedList>> {
        return communityLists.map { lists ->
            if (category.isNullOrBlank() || category == CommunityListCategories.ALL) lists
            else lists.filter { it.categoryTag.equals(category, ignoreCase = true) }
        }
    }

    override fun getCommunityListDetails(listId: String): Flow<CommunityCuratedList?> {
        return communityLists.map { lists -> lists.find { it.listId == listId } }
    }

    override suspend fun publishCustomList(params: PublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        val local = params.localList
        val curated = CommunityCuratedList(
            listId = local.listId,
            title = local.title,
            description = local.description,
            authorId = "fake_user",
            authorName = "Fake Cinephile",
            categoryTag = params.categoryTag,
            itemCount = local.items.size,
            items = local.items.map {
                CommunityCuratedListItem(
                    mediaId = it.mediaId,
                    mediaType = it.mediaType,
                    title = it.title,
                    posterImageUrl = it.posterImageUrl,
                    backdropImageUrl = it.backdropImageUrl,
                    voteAvg = it.voteAvg
                )
            },
            previewPosters = local.previewPosters,
            isMine = true
        )
        communityLists.value = communityLists.value + curated
        return Result.Success(Unit)
    }

    override suspend fun unpublishCustomList(params: UnpublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        communityLists.value = communityLists.value.filterNot { it.listId == params.listId }
        return Result.Success(Unit)
    }

    override suspend fun toggleCommunityListUpvote(params: ToggleListUpvoteParams): Result<Unit, Failure.CoreFailure> {
        communityLists.value = communityLists.value.map {
            if (it.listId == params.listId) {
                val newUpvoted = !it.isUpvotedByMe
                it.copy(
                    isUpvotedByMe = newUpvoted,
                    upvotesCount = it.upvotesCount + if (newUpvoted) 1L else -1L
                )
            } else it
        }
        return Result.Success(Unit)
    }

    override suspend fun recordListClone(listId: String): Result<Unit, Failure.CoreFailure> {
        communityLists.value = communityLists.value.map {
            if (it.listId == listId) {
                it.copy(isClonedByMe = true, clonesCount = it.clonesCount + 1L)
            } else it
        }
        return Result.Success(Unit)
    }
}
