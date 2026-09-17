package com.ssverma.feature.account.ui.profile.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.asSuccessOrErrorUiState
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.FilterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferredGenresViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreferredGenresUiState())
    val uiState: StateFlow<PreferredGenresUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PreferredGenresUiEffect>()
    val uiEffect: SharedFlow<PreferredGenresUiEffect> = _uiEffect.asSharedFlow()

    init {
        loadGenres()
        observeSavedGenres()
    }

    private fun observeSavedGenres() {
        viewModelScope.launch {
            appConfigRepository.userSeededGenres.collectLatest { savedIds ->
                _uiState.update { current ->
                    if (current.selectedGenreIds.isEmpty()) {
                        current.copy(selectedGenreIds = savedIds)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(genresState = UiState.Loading) }
            val result = filterRepository.fetchMovieGenres()
            _uiState.update {
                it.copy(genresState = result.asSuccessOrErrorUiState())
            }
        }
    }

    fun toggleGenre(genreId: Int) {
        _uiState.update { current ->
            val updated = if (genreId in current.selectedGenreIds) {
                current.selectedGenreIds - genreId
            } else {
                if (current.isMaxReached) {
                    current.selectedGenreIds
                } else {
                    current.selectedGenreIds + genreId
                }
            }
            current.copy(selectedGenreIds = updated)
        }
    }

    fun savePreferences() {
        val current = _uiState.value
        if (!current.canSave) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            appConfigRepository.updateSeededGenres(current.selectedGenreIds)
            _uiState.update { it.copy(isSaving = false) }
            _uiEffect.emit(PreferredGenresUiEffect.GenresSaved)
        }
    }

    companion object {
        val FallbackGenres = listOf(
            Genre(id = 28, name = "Action"),
            Genre(id = 12, name = "Adventure"),
            Genre(id = 16, name = "Animation"),
            Genre(id = 35, name = "Comedy"),
            Genre(id = 80, name = "Crime"),
            Genre(id = 99, name = "Documentary"),
            Genre(id = 18, name = "Drama"),
            Genre(id = 14, name = "Fantasy"),
            Genre(id = 27, name = "Horror"),
            Genre(id = 9648, name = "Mystery"),
            Genre(id = 10749, name = "Romance"),
            Genre(id = 878, name = "Sci-Fi"),
            Genre(id = 53, name = "Thriller")
        )
    }
}
