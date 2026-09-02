package com.ssverma.feature.movie.ui.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.movie.imageShots
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.GetMediaReactionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.ToggleMediaReactionUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailsData(
    val movie: Movie,
    val imageShots: List<ImageShot>
)

@HiltViewModel(assistedFactory = MovieDetailsViewModel.Factory::class)
class MovieDetailsViewModel @AssistedInject constructor(
    private val application: Application,
    @Assisted val movieId: Int,
    private val movieDetailsUseCase: MovieDetailsUseCase,
    private val getMediaReactionsUseCase: GetMediaReactionsUseCase,
    private val toggleMediaReactionUseCase: ToggleMediaReactionUseCase,
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val editCommentUseCase: EditCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val toggleCommentUpvoteUseCase: ToggleCommentUpvoteUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val getDiaryEntriesUseCase: GetDiaryEntriesUseCase,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase,
    val appConfigRepository: AppConfigRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailsViewModel
    }

    private val _uiState = MutableStateFlow<UiState<MovieDetailsData, MovieFailure>>(UiState.Idle)
    val uiState: StateFlow<UiState<MovieDetailsData, MovieFailure>> = _uiState.asStateFlow()

    val imageShots: StateFlow<List<ImageShot>> = uiState
        .map { (it as? UiState.Success)?.data?.imageShots ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val watchProviderRegion: StateFlow<String> = appConfigRepository.watchProviderRegion

    val mediaReactions: StateFlow<MediaReactions> = getMediaReactionsUseCase(
        mediaType = MediaType.Movie,
        mediaId = movieId
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaReactions.empty(mediaType = MediaType.Movie, mediaId = movieId)
    )

    private val discussionTarget = DiscussionTarget.movie(movieId)

    val discussions: StateFlow<List<Comment>> =
        getDiscussionsUseCase(discussionTarget).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val diaryEntries: StateFlow<List<DiaryEntry>> =
        getDiaryEntriesUseCase.forMedia(movieId, MediaType.Movie).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
        }
    }

    init {
        fetchMovieDetails()
    }

    fun fetchMovieDetails() {
        _uiState.update { UiState.Loading }

        val config = MovieDetailsConfig(movieId = movieId)

        viewModelScope.launch {
            val result = movieDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(
                        MovieDetailsData(
                            movie = result.data,
                            imageShots = result.data.imageShots()
                        )
                    )
                }
            }
        }
    }

    fun onReactionTagClicked(tag: MediaReactionTag) {
        viewModelScope.launch {
            toggleMediaReactionUseCase(
                mediaType = MediaType.Movie,
                mediaId = movieId,
                tag = tag
            )
        }
    }

    fun toggleMediaReaction(tag: MediaReactionTag) = onReactionTagClicked(tag)

    fun postComment(content: String, isSpoiler: Boolean) {
        viewModelScope.launch {
            val movie = (_uiState.value as? UiState.Success)?.data?.movie
            postCommentUseCase(
                PostCommentParams(
                    target = discussionTarget,
                    content = content,
                    isSpoiler = isSpoiler,
                    mediaTitle = movie?.title,
                    posterImageUrl = movie?.posterImageUrl,
                    backdropImageUrl = movie?.backdropImageUrl
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

    fun openYoutubeApp(videoId: String) {
        application.dispatchYoutubeIntent(videoId = videoId)
    }

    fun onPlayTrailerClicked(movie: Movie) {
        movie.primaryTrailer?.let {
            openYoutubeApp(it.key)
        }
    }
}
