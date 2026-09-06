package com.ssverma.feature.tv.ui.details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ThreadFilter
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.FilterAndSortCommentsUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import com.ssverma.shared.ui.component.community.CommentUiModel
import com.ssverma.shared.ui.component.community.toUiModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TvShowDiscussionsViewModel.Factory::class)
class TvShowDiscussionsViewModel @AssistedInject constructor(
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val filterAndSortCommentsUseCase: FilterAndSortCommentsUseCase,
    @ApplicationContext private val context: Context,
    @Assisted("tvShowId") private val tvShowId: Int,
    @Assisted("tvShowTitle") private val tvShowTitle: String?,
    @Assisted("posterImageUrl") private val posterImageUrl: String?,
    @Assisted("backdropImageUrl") private val backdropImageUrl: String?
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("tvShowId") tvShowId: Int,
            @Assisted("tvShowTitle") tvShowTitle: String?,
            @Assisted("posterImageUrl") posterImageUrl: String? = null,
            @Assisted("backdropImageUrl") backdropImageUrl: String? = null
        ): TvShowDiscussionsViewModel
    }

    private val discussionTarget = DiscussionTarget.tvShow(tvShowId)

    val title: String? = tvShowTitle
    val poster: String? = posterImageUrl

    private val _selectedFilter = MutableStateFlow(ThreadFilter.ALL)
    val selectedFilter: StateFlow<ThreadFilter> = _selectedFilter.asStateFlow()

    private val _locallyReportedCommentIds = MutableStateFlow<Set<String>>(emptySet())

    val discussions: StateFlow<List<Comment>> = getDiscussionsUseCase(discussionTarget).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiComments: StateFlow<List<CommentUiModel>> = combine(
        getDiscussionsUseCase(discussionTarget),
        _selectedFilter,
        _locallyReportedCommentIds
    ) { rawComments, filter, reportedIds ->
        filterAndSortCommentsUseCase(
            comments = rawComments,
            filter = filter,
            excludedCommentIds = reportedIds
        ).map { comment ->
            comment.toUiModel(context = context)
        }
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onFilterSelected(filter: ThreadFilter) {
        _selectedFilter.value = filter
    }

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
                    mediaTitle = tvShowTitle,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl
                )
            )
        }
    }

    fun postComment(args: PostCommentArgs) {
        postComment(
            content = args.content,
            isSpoiler = args.isSpoiler,
            parentId = args.parentId,
            replyToAuthorName = args.replyToAuthor
        )
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

    fun editComment(args: EditCommentArgs) {
        editComment(
            commentId = args.commentId,
            newContent = args.newContent,
            isSpoiler = args.isSpoiler
        )
    }

    fun reportComment(
        commentId: String,
        reason: String
    ) {
        _locallyReportedCommentIds.update { it + commentId }
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

    fun reportComment(args: ReportCommentArgs) {
        reportComment(
            commentId = args.commentId,
            reason = args.reason
        )
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
