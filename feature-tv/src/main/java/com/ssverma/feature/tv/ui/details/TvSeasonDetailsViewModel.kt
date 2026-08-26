package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.usecase.TvSeasonUseCase
import com.ssverma.feature.tv.ui.common.TvSeasonUiState
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

@HiltViewModel(assistedFactory = TvSeasonDetailsViewModel.Factory::class)
class TvSeasonDetailsViewModel @AssistedInject constructor(
    @Assisted("tvShowId") val tvShowId: Int,
    @Assisted("seasonNumber") val seasonNumber: Int,
    @Assisted("tvShowTitle") val tvShowTitle: String? = null,
    @Assisted("tvShowPosterPath") val tvShowPosterPath: String? = null,
    private val tvSeasonUseCase: TvSeasonUseCase,
    private val traktAuthManager: TraktAuthManager,
    private val traktSyncRepository: TraktSyncRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("tvShowId") tvShowId: Int,
            @Assisted("seasonNumber") seasonNumber: Int,
            @Assisted("tvShowTitle") tvShowTitle: String? = null,
            @Assisted("tvShowPosterPath") tvShowPosterPath: String? = null
        ): TvSeasonDetailsViewModel
    }

    private val _uiState = MutableStateFlow<TvSeasonUiState>(UiState.Idle)
    val uiState: StateFlow<TvSeasonUiState> = _uiState.asStateFlow()

    val watchedEpisodes: StateFlow<Set<Int>> = traktSyncRepository
        .getWatchedEpisodesFlow(tvShowId, seasonNumber)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    init {
        fetchTvSeason()
    }

    fun fetchTvSeason() {
        _uiState.update { UiState.Loading }

        viewModelScope.launch {
            val tvSeasonConfig = TvSeasonConfig(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber
            )

            val result = tvSeasonUseCase(tvSeasonConfig)

            _uiState.update {
                when (result) {
                    is Result.Error -> UiState.Error(result.error)
                    is Result.Success -> UiState.Success(result.data)
                }
            }
        }
    }

    fun toggleEpisodeWatched(episodeNumber: Int) {
        viewModelScope.launch {
            val token = (traktAuthManager.authState.value as? TraktAuthState.Connected)?.accessToken
            val seasonData = (_uiState.value as? UiState.Success)?.data
            val isCurrentlyWatched = episodeNumber in watchedEpisodes.value
            val targetEpNumber = if (isCurrentlyWatched) episodeNumber else episodeNumber + 1
            val targetEpTitle =
                seasonData?.episodes?.firstOrNull { it.episodeNumber == targetEpNumber }?.title
                    ?: seasonData?.episodes?.firstOrNull { it.episodeNumber == episodeNumber }?.title
            val totalAired = seasonData?.episodes?.size ?: 10
            val effectiveShowTitle = if (!tvShowTitle.isNullOrBlank()) {
                tvShowTitle
            } else {
                seasonData?.title.orEmpty()
            }

            traktSyncRepository.markEpisodeWatched(
                accessToken = token,
                showTmdbId = tvShowId,
                season = seasonNumber,
                episode = episodeNumber,
                showTitle = effectiveShowTitle,
                showPosterPath = tvShowPosterPath ?: seasonData?.posterImageUrl,
                episodeTitle = targetEpTitle,
                totalAired = totalAired
            )
        }
    }

    fun markSeasonWatched(episodeNumbers: List<Int>) {
        viewModelScope.launch {
            val token = (traktAuthManager.authState.value as? TraktAuthState.Connected)?.accessToken
            val seasonData = (_uiState.value as? UiState.Success)?.data
            val totalAired = seasonData?.episodes?.size ?: 10
            val effectiveShowTitle = if (!tvShowTitle.isNullOrBlank()) {
                tvShowTitle
            } else {
                seasonData?.title.orEmpty()
            }

            traktSyncRepository.markSeasonWatched(
                accessToken = token,
                showTmdbId = tvShowId,
                season = seasonNumber,
                episodeNumbers = episodeNumbers,
                showTitle = effectiveShowTitle,
                showPosterPath = tvShowPosterPath ?: seasonData?.posterImageUrl,
                totalAired = totalAired
            )
        }
    }
}
