package com.ssverma.feature.movie.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.core.domain.model.Review
import com.ssverma.feature.movie.domain.usecase.MovieReviewsPaginatedUseCase
import com.ssverma.feature.movie.navigation.MovieReviewsDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieReviewsViewModel @Inject constructor(
    reviewsUseCase: MovieReviewsPaginatedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = savedStateHandle.get<Int>(MovieReviewsDestination.MovieId) ?: 0

    val pagedReviews: Flow<PagingData<Review>> =
        reviewsUseCase(movieId).cachedIn(viewModelScope)
}