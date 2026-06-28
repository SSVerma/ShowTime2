package com.ssverma.feature.tv.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.feature.tv.domain.usecase.TvShowReviewsPaginatedUseCase
import com.ssverma.shared.domain.model.Review
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow

@HiltViewModel(assistedFactory = TvShowReviewsViewModel.Factory::class)
class TvShowReviewsViewModel @AssistedInject constructor(
    tvShowReviewsUseCase: TvShowReviewsPaginatedUseCase,
    @Assisted private val tvShowId: Int
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(tvShowId: Int): TvShowReviewsViewModel
    }

    val pagedReviews: Flow<PagingData<Review>> =
        tvShowReviewsUseCase(tvShowId).cachedIn(viewModelScope)
}
