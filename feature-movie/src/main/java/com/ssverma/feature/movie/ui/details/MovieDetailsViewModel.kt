package com.ssverma.feature.movie.ui.details

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.billing.BillingRepository
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchYoutubeIntent
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.usecase.MovieCollectionUseCase
import com.ssverma.feature.movie.domain.usecase.MovieDetailsUseCase
import com.ssverma.feature.movie.ui.details.component.AiringReminderGateConfig
import com.ssverma.feature.movie.ui.details.component.CommunityCommentsGateConfig
import com.ssverma.shared.ads.gate.FeatureGateConfig
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.ReportCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.movie.MovieCollection
import com.ssverma.shared.domain.model.movie.imageShots
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.AffiliateRepository
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.usecase.community.DeleteCommentUseCase
import com.ssverma.shared.domain.usecase.community.EditCommentUseCase
import com.ssverma.shared.domain.usecase.community.GetDiscussionsUseCase
import com.ssverma.shared.domain.usecase.community.GetMediaReactionsUseCase
import com.ssverma.shared.domain.usecase.community.PostCommentResult
import com.ssverma.shared.domain.usecase.community.PostCommentUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommentUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommentUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.ToggleMediaReactionUseCase
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import com.ssverma.shared.domain.usecase.reminder.RemoveAiringReminderUseCase
import com.ssverma.shared.domain.usecase.reminder.ScheduleAiringReminderUseCase
import com.ssverma.shared.domain.usecase.reminder.ScheduleReminderResult
import com.ssverma.shared.domain.utils.ReminderTimeCalculator
import com.ssverma.shared.domain.utils.formatLocally
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailsData(
    val movie: Movie,
    val imageShots: List<ImageShot>
)

data class ProviderActionPayload(
    val provider: ProviderInfo,
    val category: String
)

@HiltViewModel(assistedFactory = MovieDetailsViewModel.Factory::class)
class MovieDetailsViewModel @AssistedInject constructor(
    private val application: Application,
    @Assisted val movieId: Int,
    private val movieDetailsUseCase: MovieDetailsUseCase,
    private val movieCollectionUseCase: MovieCollectionUseCase,
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
    val affiliateRepository: AffiliateRepository,
    val reminderRepository: ReminderRepository,
    private val scheduleAiringReminderUseCase: ScheduleAiringReminderUseCase,
    private val removeAiringReminderUseCase: RemoveAiringReminderUseCase,
    val billingRepository: BillingRepository,
    val rewardManager: RewardManager,
    val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieDetailsViewModel
    }

    val hasReminder: StateFlow<Boolean> = reminderRepository
        .getReminderForMedia(movieId, MediaType.Movie)
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _activeGateConfig = MutableStateFlow<FeatureGateConfig?>(null)
    val activeGateConfig: StateFlow<FeatureGateConfig?> = _activeGateConfig.asStateFlow()

    val isQuotaGateVisible: StateFlow<Boolean> = _activeGateConfig
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    private val _reminderSnackbarEvent = MutableStateFlow<String?>(null)
    val reminderSnackbarEvent: StateFlow<String?> = _reminderSnackbarEvent.asStateFlow()

    private var pendingCommentParams: PostCommentParams? = null

    fun dismissQuotaGate() {
        _activeGateConfig.value = null
        _isAdLoading.value = false
        pendingCommentParams = null
    }

    fun clearReminderSnackbarEvent() {
        _reminderSnackbarEvent.value = null
    }

    private val _isReminderSheetVisible = MutableStateFlow(false)
    val isReminderSheetVisible: StateFlow<Boolean> = _isReminderSheetVisible.asStateFlow()

    val reminderLeadDays: StateFlow<Int> = appConfigRepository.reminderLeadDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val reminderNotificationHour: StateFlow<Int> = appConfigRepository.reminderNotificationHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 9)

    val reminderNotificationMinute: StateFlow<Int> = appConfigRepository.reminderNotificationMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun openReminderSheet() {
        _isReminderSheetVisible.value = true
    }

    fun dismissReminderSheet() {
        _isReminderSheetVisible.value = false
    }

    fun scheduleReminder(
        movie: Movie,
        leadDays: Int,
        hour: Int,
        minute: Int
    ) {
        viewModelScope.launch {
            _isReminderSheetVisible.value = false
            val targetReleaseDate = movie.nextFutureReleaseDate ?: movie.releaseDate
            if (targetReleaseDate == null) {
                _reminderSnackbarEvent.value =
                    "No upcoming release date found for ${movie.title}"
                return@launch
            }

            val providerName = movie.watchProviders.values
                .firstNotNullOfOrNull {
                    it.flatrate.firstOrNull()?.providerName
                        ?: it.rent.firstOrNull()?.providerName
                }

            val result = scheduleAiringReminderUseCase(
                mediaId = movie.id,
                mediaType = MediaType.Movie,
                mediaTitle = movie.title,
                posterImageUrl = movie.posterImageUrl.orEmpty(),
                targetAirDate = targetReleaseDate,
                leadDays = leadDays,
                hour = hour,
                minute = minute,
                isProActive = billingRepository.isProActive.value,
                providerName = providerName
            )

            when (result) {
                is ScheduleReminderResult.Success -> {
                    _reminderSnackbarEvent.value = "Reminder set for ${movie.title}!"
                }

                is ScheduleReminderResult.QuotaExceeded -> {
                    _activeGateConfig.value = AiringReminderGateConfig
                    rewardedAdManager.loadAd()
                }

                is ScheduleReminderResult.TimePassed -> {
                    _reminderSnackbarEvent.value = "Release date has already passed"
                }

                is ScheduleReminderResult.NoSchedule -> {
                    _reminderSnackbarEvent.value =
                        "No upcoming release date found for ${movie.title}"
                }

                is ScheduleReminderResult.Error -> {
                    _reminderSnackbarEvent.value = result.message ?: "Failed to set reminder"
                }
            }
        }
    }

    fun removeReminder(movie: Movie) {
        viewModelScope.launch {
            _isReminderSheetVisible.value = false
            removeAiringReminderUseCase(movie.id, MediaType.Movie)
            _reminderSnackbarEvent.value = "Reminder removed for ${movie.title}"
        }
    }

    fun toggleReminder(movie: Movie) {
        viewModelScope.launch {
            if (hasReminder.value) {
                removeReminder(movie)
            } else {
                val hour = appConfigRepository.reminderNotificationHour.first()
                val minute = appConfigRepository.reminderNotificationMinute.first()
                val leadDays = appConfigRepository.reminderLeadDays.first()
                scheduleReminder(movie, leadDays, hour, minute)
            }
        }
    }

    fun onWatchAdForActiveGate(activity: Activity, movie: Movie) {
        val gateConfig = _activeGateConfig.value ?: return
        _isAdLoading.value = true
        rewardedAdManager.showRewardedAdIfReady(
            activity = activity,
            onAdDismissed = { _isAdLoading.value = false }
        ) {
            viewModelScope.launch {
                _isAdLoading.value = false
                when (gateConfig) {
                    AiringReminderGateConfig -> {
                        rewardManager.grantReminderPass()
                        _activeGateConfig.value = null
                        toggleReminder(movie)
                    }

                    CommunityCommentsGateConfig -> {
                        rewardManager.grantCommentPass()
                        _activeGateConfig.value = null
                        pendingCommentParams?.let { params ->
                            postCommentUseCase(params)
                            pendingCommentParams = null
                        }
                    }

                    else -> {
                        _activeGateConfig.value = null
                    }
                }
            }
        }
    }

    fun onWatchAdForReminderPass(activity: Activity, movie: Movie) =
        onWatchAdForActiveGate(activity, movie)

    private val _selectedProviderForAction = MutableStateFlow<ProviderActionPayload?>(null)
    val selectedProviderForAction: StateFlow<ProviderActionPayload?> =
        _selectedProviderForAction.asStateFlow()

    fun onProviderSelectedForAction(provider: ProviderInfo, category: String) {
        _selectedProviderForAction.value =
            ProviderActionPayload(provider = provider, category = category)
    }

    fun dismissProviderAction() {
        _selectedProviderForAction.value = null
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

    private val _movieCollection = MutableStateFlow<MovieCollection?>(null)
    val movieCollection: StateFlow<MovieCollection?> = _movieCollection.asStateFlow()

    private val _isCollectionLoading = MutableStateFlow(false)
    val isCollectionLoading: StateFlow<Boolean> = _isCollectionLoading.asStateFlow()

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
                    is Result.Success -> {
                        val movie = result.data
                        movie.movieCollection?.let { collection ->
                            _movieCollection.value = collection
                            fetchCollectionDetails(collection.id)
                        }
                        UiState.Success(
                            MovieDetailsData(
                                movie = movie,
                                imageShots = movie.imageShots()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun fetchCollectionDetails(collectionId: Int) {
        viewModelScope.launch {
            _isCollectionLoading.value = true
            val result = movieCollectionUseCase(collectionId)
            if (result is Result.Success) {
                _movieCollection.value = result.data
            }
            _isCollectionLoading.value = false
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
            val params = PostCommentParams(
                target = discussionTarget,
                content = content,
                isSpoiler = isSpoiler,
                mediaTitle = movie?.title,
                posterImageUrl = movie?.posterImageUrl,
                backdropImageUrl = movie?.backdropImageUrl,
                isProUser = billingRepository.isProActive.value
            )
            when (val result = postCommentUseCase(params)) {
                is PostCommentResult.Success -> {
                    pendingCommentParams = null
                }

                is PostCommentResult.QuotaExceeded -> {
                    pendingCommentParams = params
                    _activeGateConfig.value = CommunityCommentsGateConfig
                    rewardedAdManager.loadAd()
                }

                is PostCommentResult.Error -> {
                    pendingCommentParams = null
                }
            }
        }
    }

    fun postComment(args: PostCommentArgs) = postComment(args.content, args.isSpoiler)

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

    fun editComment(args: EditCommentArgs) =
        editComment(args.commentId, args.newContent, args.isSpoiler)

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

    fun reportComment(args: ReportCommentArgs) = reportComment(args.commentId, args.reason)

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
