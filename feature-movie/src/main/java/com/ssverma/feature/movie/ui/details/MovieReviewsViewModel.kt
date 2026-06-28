package com.ssverma.feature.movie.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.feature.movie.domain.usecase.MovieReviewsPaginatedUseCase
import com.ssverma.shared.domain.model.Review
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow

@HiltViewModel(assistedFactory = MovieReviewsViewModel.Factory::class)
class MovieReviewsViewModel @AssistedInject constructor(
    reviewsUseCase: MovieReviewsPaginatedUseCase,
    @Assisted private val movieId: Int
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(movieId: Int): MovieReviewsViewModel
    }

    val pagedReviews: Flow<PagingData<Review>> =
        reviewsUseCase(movieId).cachedIn(viewModelScope)
}
