package com.ssverma.showtime.ui.tv

import android.app.Application
import androidx.lifecycle.*
import com.ssverma.showtime.api.AppendableQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.domain.model.imageShots
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

private val tvShowDetailsAppendable = QueryMultiValue.andBuilder()
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Credits)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Images)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Videos)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Reviews)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Similar)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Keywords)
    .and(TmdbApiTiedConstants.AppendableResponseTypes.Recommendations)

@HiltViewModel
class TvShowDetailsViewModel @Inject constructor(
    tvRepository: TvRepository,
    savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    val tvShowId = savedStateHandle.get<Int>(AppDestination.TvShowDetails.ArgTvShowId) ?: 0

    private val _liveImageShots = MediatorLiveData<List<ImageShot>>()
    val imageShots: LiveData<List<ImageShot>> get() = _liveImageShots

    val liveTvShowDetails: LiveData<Result<TvShow>> = liveData {
        tvRepository.fetchTvShowDetails(
            tvShowId = tvShowId,
            queryMap = AppendableQueryMap.of(appendToResponse = tvShowDetailsAppendable)
        ).collect {
            emit(it)

            if (it is Result.Success) {
                viewModelScope.launch {
                    val imageShots = it.data.imageShots()
                    _liveImageShots.postValue(imageShots)
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