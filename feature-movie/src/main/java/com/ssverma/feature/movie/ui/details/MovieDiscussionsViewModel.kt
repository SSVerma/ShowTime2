package com.ssverma.feature.movie.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MovieDiscussionsViewModel.Factory::class)
class MovieDiscussionsViewModel @AssistedInject constructor(
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    @Assisted("movieId") private val movieId: Int,
    @Assisted("movieTitle") private val movieTitle: String?,
    @Assisted("posterImageUrl") private val posterImageUrl: String?,
    @Assisted("backdropImageUrl") private val backdropImageUrl: String?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("movieId") movieId: Int,
            @Assisted("movieTitle") movieTitle: String?,
            @Assisted("posterImageUrl") posterImageUrl: String? = null,
            @Assisted("backdropImageUrl") backdropImageUrl: String? = null
        ): MovieDiscussionsViewModel
    }

    private val discussionTarget = DiscussionTarget.movie(movieId)

    val title: String? = movieTitle

    val discussions: StateFlow<List<Comment>> = getDiscussionsUseCase(discussionTarget).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun postComment(
        content: String,
        isSpoiler: Boolean,
        parentId: String? = null,
        replyToAuthorName: String? = null
    ) {
        viewModelScope.launch {
            postCommentUseCase(
                PostCommentParams(
                    target = discussionTarget,
                    content = content,
                    isSpoiler = isSpoiler,
                    parentId = parentId,
                    replyToAuthorName = replyToAuthorName,
                    mediaTitle = movieTitle,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl
                )
            )
        }
    }

    fun editComment(
        commentId: String,
        newContent: String,
        isSpoiler: Boolean
    ) {
        viewModelScope.launch {
            editCommentUseCase(
                EditCommentParams(
                    target = discussionTarget,
                    commentId = commentId,
                    newContent = newContent,
                    isSpoiler = isSpoiler
                )
            )
        }
    }

    fun reportComment(
        commentId: String,
        reason: String
    ) {
        viewModelScope.launch {
            reportCommentUseCase(
                ReportCommentParams(
                    target = discussionTarget,
                    commentId = commentId,
                    reason = reason
                )
            )
        }
    }

    fun toggleCommentUpvote(commentId: String) {
        viewModelScope.launch {
            toggleCommentUpvoteUseCase(
                ToggleCommentUpvoteParams(
                    target = discussionTarget,
                    commentId = commentId
                )
            )
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            deleteCommentUseCase(
                DeleteCommentParams(
                    target = discussionTarget,
                    commentId = commentId
                )
            )
        }
    }
}
