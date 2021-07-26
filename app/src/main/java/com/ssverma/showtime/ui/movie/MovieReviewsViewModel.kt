package com.ssverma.showtime.ui.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.data.repository.MovieRepository
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieReviewsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = savedStateHandle.get<Int>(AppDestination.MovieReviews.MovieId) ?: 0

    val pagedReviews: Flow<PagingData<Review>> =
        movieRepository.fetchMovieReviewsGradually(movieId = movieId).cachedIn(viewModelScope)

}