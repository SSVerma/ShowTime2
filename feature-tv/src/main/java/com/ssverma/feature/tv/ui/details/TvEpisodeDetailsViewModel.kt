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
import com.ssverma.shared.domain.repository.TraktSyncRepository
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
    private val traktSyncRepository: TraktSyncRepository
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
}
