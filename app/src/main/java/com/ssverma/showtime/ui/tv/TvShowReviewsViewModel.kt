package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.core.domain.model.Review
import com.ssverma.showtime.domain.usecase.tv.TvShowReviewsPaginatedUseCase
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class TvShowReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvShowReviewsUseCase: TvShowReviewsPaginatedUseCase
) : ViewModel() {

    private val tvShowId: Int = savedStateHandle
        .get<Int>(AppDestination.TvShowReviews.TvShowId) ?: 0

    val pagedReviews: Flow<PagingData<Review>> =
        tvShowReviewsUseCase(tvShowId).cachedIn(viewModelScope)

}