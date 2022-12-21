package com.ssverma.feature.account.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaStatsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _mediaStats: MutableStateFlow<MediaStatsUiState> =
        MutableStateFlow(MediaStatsUiState.Loading)

    val mediaStats = _mediaStats.asStateFlow()

    fun fetchMediaStats(mediaType: MediaType, mediaId: Int, forceRefresh: Boolean = false) {
        val mediaStats = (_mediaStats.value as? MediaStatsUiState.Success)?.mediaStats

        if (!forceRefresh && mediaStats?.mediaId == mediaId) {
            return
        }

        _mediaStats.value = MediaStatsUiState.Loading

        viewModelScope.launch {
            val sessionId: String = authManager.sessionIdOrNull() ?: kotlin.run {
                _mediaStats.value = MediaStatsUiState.Unauthorized
                return@launch
            }

            val result = accountRepository.fetchAccountStats(
                sessionId = sessionId,
                mediaType = mediaType,
                mediaId = mediaId
            )

            _mediaStats.value = when (result) {
                is Result.Error -> MediaStatsUiState.Error(result.error)
                is Result.Success -> MediaStatsUiState.Success(result.data)
            }
        }
    }

    fun toggleMediaFavoriteStatus(
        mediaType: MediaType,
        mediaId: Int,
        isCurrentFavorite: Boolean
    ) {
        _mediaStats.value = MediaStatsUiState.Loading

        viewModelScope.launch {
            val sessionId: String = authManager.sessionIdOrNull() ?: kotlin.run {
                _mediaStats.value = MediaStatsUiState.Unauthorized
                return@launch
            }

            val result = accountRepository.toggleMediaFavoriteStatus(
                sessionId = sessionId,
                mediaType = mediaType,
                mediaId = mediaId,
                favorite = !isCurrentFavorite
            )

            when (result) {
                is Result.Error -> {
                    _mediaStats.value = MediaStatsUiState.Error(result.error)
                }
                is Result.Success -> {
                    // refresh
                    fetchMediaStats(mediaType = mediaType, mediaId = mediaId, forceRefresh = true)
                }
            }
        }
    }

    fun toggleMediaWatchlistStatus(
        mediaType: MediaType,
        mediaId: Int,
        isCurrentInWatchlist: Boolean
    ) {
        _mediaStats.value = MediaStatsUiState.Loading

        viewModelScope.launch {
            val sessionId: String = authManager.sessionIdOrNull() ?: return@launch

            val result = accountRepository.toggleMediaWatchlistStatus(
                sessionId = sessionId,
                mediaType = mediaType,
                mediaId = mediaId,
                inWatchlist = !isCurrentInWatchlist
            )

            when (result) {
                is Result.Error -> {
                    _mediaStats.value = MediaStatsUiState.Error(result.error)
                }
                is Result.Success -> {
                    // refresh
                    fetchMediaStats(mediaType = mediaType, mediaId = mediaId, forceRefresh = true)
                }
            }
        }
    }
}