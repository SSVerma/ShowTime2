package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class TvShowReviewsViewModel @Inject constructor(
    tvRepository: TvRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvShowId: Int = savedStateHandle
        .get<Int>(AppDestination.TvShowReviews.TvShowId) ?: 0

    val pagedReviews: Flow<PagingData<Review>> =
        tvRepository.fetchTvShowReviewsGradually(tvShowId = tvShowId).cachedIn(viewModelScope)

}