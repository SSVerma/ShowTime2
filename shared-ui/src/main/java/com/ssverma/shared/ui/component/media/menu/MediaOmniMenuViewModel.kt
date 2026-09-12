package com.ssverma.shared.ui.component.media.menu

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.billing.BillingRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.repository.ReminderQuotaManager
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.repository.ReminderToggleResult
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MediaOmniMenuViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val saveDiaryEntryUseCase: SaveDiaryEntryUseCase,
    private val getDiaryEntriesUseCase: GetDiaryEntriesUseCase,
    private val reminderRepository: ReminderRepository,
    private val reminderQuotaManager: ReminderQuotaManager,
    private val rewardedAdManager: RewardedAdManager,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val isProPaymentEnabled: StateFlow<Boolean> = billingRepository.isBillingEnabled

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    fun preloadRewardedAd() {
        rewardedAdManager.loadAd()
    }

    fun dismissQuotaGate() {
        _isAdLoading.value = false
    }

    fun onWatchAdForReminderPass(activity: Activity, onRewardGranted: () -> Unit) {
        _isAdLoading.value = true
        rewardedAdManager.showRewardedAdIfReady(
            activity = activity,
            onAdDismissed = { _isAdLoading.value = false }
        ) {
            viewModelScope.launch {
                _isAdLoading.value = false
                reminderQuotaManager.grantReminderPass()
                onRewardGranted()
            }
        }
    }

    fun hasReminder(mediaId: Int, mediaType: MediaType): Flow<Boolean> =
        reminderRepository.getReminderForMedia(mediaId, mediaType).map { it != null }

    fun toggleReminder(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        targetAirDate: LocalDate? = null,
        onResult: ((ReminderToggleResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = reminderRepository.toggleMediaReminder(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                targetAirDate = targetAirDate
            )
            onResult?.invoke(result)
        }
    }

    val customLists: Flow<List<CustomList>> = libraryRepository.getCustomListsFlow()

    fun getCustomListIdsForMedia(mediaId: Int): Flow<List<String>> =
        libraryRepository.getCustomListIdsForMediaFlow(mediaId)

    fun isInWatchlist(mediaId: Int): Flow<Boolean> =
        libraryRepository.isInWatchlistFlow(mediaId)

    fun isFavorite(mediaId: Int): Flow<Boolean> =
        libraryRepository.isFavoriteFlow(mediaId)

    fun isWatched(mediaId: Int): Flow<Boolean> =
        libraryRepository.isWatchedFlow(mediaId)

    fun isMediaActionActive(mediaId: Int): Flow<Boolean> =
        libraryRepository.isMediaActionActiveFlow(mediaId)

    fun getDiaryEntries(mediaId: Int, mediaType: MediaType): Flow<List<DiaryEntry>> =
        getDiaryEntriesUseCase.forMedia(mediaId, mediaType)

    fun toggleWatchlist(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleWatchlist(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            onResult?.invoke(added)
        }
    }

    fun toggleFavorite(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        backdropImageUrl: String,
        voteAvg: Float,
        releaseDate: String,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleFavorite(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                releaseDate = releaseDate
            )
            onResult?.invoke(added)
        }
    }

    fun toggleWatched(
        mediaId: Int,
        mediaType: MediaType,
        title: String,
        posterImageUrl: String,
        voteAvg: Float,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val added = libraryRepository.toggleWatchHistory(
                mediaId = mediaId,
                mediaType = mediaType,
                title = title,
                posterImageUrl = posterImageUrl,
                voteAvg = voteAvg
            )
            onResult?.invoke(added)
        }
    }

    fun toggleMediaCustomList(
        listId: String,
        mediaId: Int,
        mediaType: MediaType,
        title: String = "",
        posterImageUrl: String = "",
        backdropImageUrl: String = "",
        voteAvg: Float = 0f,
        isCurrentlyInList: Boolean,
        onResult: ((added: Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            if (isCurrentlyInList) {
                libraryRepository.removeMediaFromCustomList(listId, mediaId)
                onResult?.invoke(false)
            } else {
                libraryRepository.addMediaToCustomList(
                    listId = listId,
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg
                )
                onResult?.invoke(true)
            }
        }
    }

    fun saveDiaryEntry(
        entry: DiaryEntry,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            saveDiaryEntryUseCase(entry)
            onComplete?.invoke()
        }
    }
}
