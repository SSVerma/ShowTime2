package com.ssverma.feature.tv.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.shared.domain.model.Review
import com.ssverma.feature.tv.domain.usecase.TvShowReviewsPaginatedUseCase
import com.ssverma.feature.tv.navigation.TvShowReviewsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class TvShowReviewsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvShowReviewsUseCase: TvShowReviewsPaginatedUseCase
) : ViewModel() {

    private val tvShowId: Int = savedStateHandle
        .get<Int>(TvShowReviewsDestination.TvShowId) ?: 0

    val pagedReviews: Flow<PagingData<Review>> =
        tvShowReviewsUseCase(tvShowId).cachedIn(viewModelScope)

}