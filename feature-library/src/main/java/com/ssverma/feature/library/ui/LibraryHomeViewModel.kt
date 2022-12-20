package com.ssverma.feature.library.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LibraryHomeViewModel @Inject constructor(
    accountRepository: AccountRepository
) : ViewModel() {

    val pagedFavoriteMovies: Flow<PagingData<Movie>> by lazy {
        accountRepository.fetchFavoriteMoviesGradually()
            .cachedIn(viewModelScope)
    }

    val pagedWatchlistMovies: Flow<PagingData<Movie>> by lazy {
        accountRepository.fetchWatchlistMoviesGradually()
            .cachedIn(viewModelScope)
    }

    val pagedFavoriteTvShows: Flow<PagingData<TvShow>> by lazy {
        accountRepository.fetchFavoriteTvShowsGradually()
            .cachedIn(viewModelScope)
    }

    val pagedWatchlistTvShows: Flow<PagingData<TvShow>> by lazy {
        accountRepository.fetchWatchlistTvShowsGradually()
            .cachedIn(viewModelScope)
    }
}