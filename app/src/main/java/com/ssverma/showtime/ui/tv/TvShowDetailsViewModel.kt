package com.ssverma.showtime.ui.tv

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.model.tv.TvShowDetailsConfig
import com.ssverma.showtime.domain.model.tv.imageShots
import com.ssverma.showtime.domain.usecase.tv.TvShowDetailsUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.core.ui.UiState
import com.ssverma.showtime.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    application: Application,
    private val tvShowDetailsUseCase: TvShowDetailsUseCase
) : AndroidViewModel(application) {

    val tvShowId = savedStateHandle.get<Int>(AppDestination.TvShowDetails.ArgTvShowId) ?: 0

    private val _imageShots = mutableStateOf<List<ImageShot>>(emptyList())
    val imageShots: State<List<ImageShot>> get() = _imageShots

    var tvShowDetailsUiState by mutableStateOf<TvShowDetailsUiState>(UiState.Idle)
        private set

    init {
        fetchTvShowDetails()
    }

    fun fetchTvShowDetails(coroutineScope: CoroutineScope = viewModelScope) {
        tvShowDetailsUiState = UiState.Loading

        val config = TvShowDetailsConfig(tvShowId = tvShowId)

        coroutineScope.launch {
            val result = tvShowDetailsUseCase(config)
            tvShowDetailsUiState = when (result) {
                is Result.Error -> {
                    UiState.Error(result.error)
                }
                is Result.Success -> {
                    _imageShots.value = result.data.imageShots()
                    UiState.Success(result.data)
                }
            }
        }
    }

    fun openYoutubeApp(videoId: String) {
        AppUtils.dispatchOpenYoutubeIntent(getApplication(), videoId)
    }

    fun onPlayTrailerClicked(tvShow: TvShow) {
        tvShow.videos.firstOrNull()?.let {
            openYoutubeApp(it.key)
        }
    }
}