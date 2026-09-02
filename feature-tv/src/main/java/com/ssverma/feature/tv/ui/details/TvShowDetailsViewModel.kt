package com.ssverma.feature.tv.ui.details

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.usecase.TvShowDetailsUseCase
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
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.model.tv.imageShots
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.TraktSyncRepository
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

data class TvShowDetailsData(
    val tvShow: TvShow,
    val imageShots: List<ImageShot>
)

@HiltViewModel(assistedFactory = TvShowDetailsViewModel.Factory::class)
class TvShowDetailsViewModel @AssistedInject constructor(
    private val application: Application,
    @Assisted val tvShowId: Int,
    private val tvShowDetailsUseCase: TvShowDetailsUseCase,
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
    val appConfigRepository: AppConfigRepository,
    private val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(tvShowId: Int): TvShowDetailsViewModel
    }

    private val _uiState = MutableStateFlow<UiState<TvShowDetailsData, TvShowFailure>>(UiState.Idle)
    val uiState: StateFlow<UiState<TvShowDetailsData, TvShowFailure>> = _uiState.asStateFlow()

    val watchedSeasons: StateFlow<Set<Int>> = traktSyncRepository
        .getWatchedSeasonsFlow(tvShowId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    val seasonWatchCounts: StateFlow<Map<Int, Int>> = traktSyncRepository
        .getSeasonWatchCountsFlow(tvShowId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val diaryEntries: StateFlow<List<DiaryEntry>> =
        getDiaryEntriesUseCase.forMedia(tvShowId, MediaType.Tv).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
        }
    }

    val imageShots: StateFlow<List<ImageShot>> = uiState
        .map { (it as? UiState.Success)?.data?.imageShots ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val watchProviderRegion: StateFlow<String> = appConfigRepository.watchProviderRegion

    val mediaReactions: StateFlow<MediaReactions> = getMediaReactionsUseCase(
        mediaType = MediaType.Tv,
        mediaId = tvShowId
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MediaReactions.empty(mediaType = MediaType.Tv, mediaId = tvShowId)
    )

    private val discussionTarget = DiscussionTarget.tvShow(tvShowId)

    val discussions: StateFlow<List<Comment>> =
        getDiscussionsUseCase(discussionTarget).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        fetchTvShowDetails()
    }

    fun fetchTvShowDetails() {
        _uiState.update { UiState.Loading }

        val config = TvShowDetailsConfig(tvShowId = tvShowId)

        viewModelScope.launch {
            val result = tvShowDetailsUseCase(config)
            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(
                        TvShowDetailsData(
                            tvShow = result.data,
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
                mediaType = MediaType.Tv,
                mediaId = tvShowId,
                tag = tag
            )
        }
    }

    fun toggleSeasonWatched(tvSeason: TvSeason) {
        viewModelScope.launch {
            val token = (traktAuthManager.authState.value as? TraktAuthState.Connected)?.accessToken
            val watchedCount = seasonWatchCounts.value[tvSeason.seasonNumber] ?: 0
            val isFullyWatched = tvSeason.episodeCount > 0 && watchedCount >= tvSeason.episodeCount
            val episodeNumbers = if (isFullyWatched) {
                emptyList()
            } else {
                (1..tvSeason.episodeCount).toList()
            }
            val show = (_uiState.value as? UiState.Success)?.data?.tvShow
            val totalEpisodes =
                show?.seasons?.sumOf { it.episodeCount } ?: (tvSeason.episodeCount * 2)

            traktSyncRepository.markSeasonWatched(
                accessToken = token,
                showTmdbId = tvShowId,
                season = tvSeason.seasonNumber,
                episodeNumbers = episodeNumbers,
                showTitle = show?.title.orEmpty(),
                showPosterPath = show?.posterImageUrl,
                totalAired = totalEpisodes
            )
        }
    }

    fun openYoutubeApp(videoId: String) {
        application.dispatchYoutubeIntent(videoId = videoId)
    }

    fun postComment(content: String, isSpoiler: Boolean) {
        viewModelScope.launch {
            val tvShow = (_uiState.value as? UiState.Success)?.data?.tvShow
            postCommentUseCase(
                PostCommentParams(
                    target = discussionTarget,
                    content = content,
                    isSpoiler = isSpoiler,
                    mediaTitle = tvShow?.title,
                    posterImageUrl = tvShow?.posterImageUrl,
                    backdropImageUrl = tvShow?.backdropImageUrl
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

    fun onPlayTrailerClicked(tvShow: TvShow) {
        tvShow.primaryTrailer?.let {
            openYoutubeApp(it.key)
        }
    }
}
