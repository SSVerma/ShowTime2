package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ssverma.showtime.api.AppendableQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TvEpisodeDetailsViewModel @Inject constructor(
    tvRepository: TvRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvShowId = savedStateHandle
        .get<Int>(AppDestination.TvEpisodeDetails.ArgTvShowId) ?: 0
    private val seasonNumber = savedStateHandle
        .get<Int>(AppDestination.TvEpisodeDetails.ArgSeasonNumber) ?: 0
    private val episodeNumber = savedStateHandle
        .get<Int>(AppDestination.TvEpisodeDetails.ArgEpisodeNumber) ?: 0

    val liveEpisode = tvRepository.fetchTvEpisodeDetails(
        tvShowId = tvShowId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        queryMap = AppendableQueryMap.of(
            QueryMultiValue.andBuilder()
                .and(TmdbApiTiedConstants.AppendableResponseTypes.Credits)
                .and(TmdbApiTiedConstants.AppendableResponseTypes.Images)
        )
    ).asLiveData()

}