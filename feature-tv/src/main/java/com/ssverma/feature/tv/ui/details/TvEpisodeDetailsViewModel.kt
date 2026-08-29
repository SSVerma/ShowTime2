package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.tv.domain.model.TvEpisodeConfig
import com.ssverma.feature.tv.domain.usecase.TvEpisodeUseCase
import com.ssverma.feature.tv.ui.common.TvEpisodeUiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.repository.TraktSyncRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TvEpisodeDetailsViewModel.Factory::class)
class TvEpisodeDetailsViewModel @AssistedInject constructor(
    @Assisted("tvShowId") val tvShowId: Int,
    @Assisted("seasonNumber") val seasonNumber: Int,
    @Assisted("episodeNumber") val episodeNumber: Int,
    @Assisted("tvShowTitle") val tvShowTitle: String? = null,
    @Assisted("tvShowPosterPath") val tvShowPosterPath: String? = null,
    private val tvEpisodeUseCase: TvEpisodeUseCase,
    private val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository,
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("tvShowId") tvShowId: Int,
            @Assisted("seasonNumber") seasonNumber: Int,
            @Assisted("episodeNumber") episodeNumber: Int,
            @Assisted("tvShowTitle") tvShowTitle: String? = null,
            @Assisted("tvShowPosterPath") tvShowPosterPath: String? = null
        ): TvEpisodeDetailsViewModel
    }

    private val _uiState = MutableStateFlow<TvEpisodeUiState>(UiState.Idle)
    val uiState: StateFlow<TvEpisodeUiState> = _uiState.asStateFlow()

    val isWatched: StateFlow<Boolean> = traktSyncRepository
        .isEpisodeWatchedFlow(tvShowId, seasonNumber, episodeNumber)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val discussionTarget = DiscussionTarget.tvEpisode(
        tvShowId = tvShowId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )

    val discussions: StateFlow<List<Comment>> =
        getDiscussionsUseCase(discussionTarget).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchTvEpisode()
    }

    fun fetchTvEpisode() {
        _uiState.update { UiState.Loading }

        viewModelScope.launch {
            val tvEpisodeConfig = TvEpisodeConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber
            )

            val result = tvEpisodeUseCase(tvEpisodeConfig)

            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(result.data)
                }
            }
        }
    }

    fun toggleWatched() {
        viewModelScope.launch {
            val token = (traktAuthManager.authState.value as? TraktAuthState.Connected)?.accessToken
            val episodeData = (_uiState.value as? UiState.Success)?.data

            traktSyncRepository.markEpisodeWatched(
                accessToken = token,
                showTmdbId = tvShowId,
                season = seasonNumber,
                episode = episodeNumber,
                showTitle = tvShowTitle.orEmpty(),
                showPosterPath = tvShowPosterPath,
                episodeTitle = episodeData?.title,
                totalAired = 0
            )
        }
    }

    fun postComment(content: String, isSpoiler: Boolean) {
        viewModelScope.launch {
            val title =
                if (!tvShowTitle.isNullOrBlank()) "$tvShowTitle - S${seasonNumber}E${episodeNumber}" else "S${seasonNumber}E${episodeNumber}"
            postCommentUseCase(
                PostCommentParams(
                    target = discussionTarget,
                    content = content,
                    isSpoiler = isSpoiler,
                    mediaTitle = title,
                    posterImageUrl = tvShowPosterPath
                )
            )
        }
    }

    fun editComment(commentId: String, newContent: String, isSpoiler: Boolean) {
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

    fun reportComment(commentId: String, reason: String) {
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
