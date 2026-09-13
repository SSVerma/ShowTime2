package com.ssverma.showtime.ui.onboarding

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.backup.model.isGoogleSignInCancelled
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.repository.FilterRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository,
    private val filterRepository: FilterRepository,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
        loadGenres()
        observeGoogleUser()
        observeBackupState()
    }

    private fun observeGoogleUser() {
        viewModelScope.launch {
            backupRepository.googleUser.collectLatest { user ->
                _uiState.update { it.copy(googleUser = user) }
            }
        }
    }

    private fun observeBackupState() {
        viewModelScope.launch {
            backupRepository.lastBackupMetadata.collectLatest { metadata ->
                _uiState.update { it.copy(lastBackupMetadata = metadata) }
            }
        }
    }

    private fun loadProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProvidersLoading = true) }
            when (val result = watchProviderRepository.fetchAllMovieWatchProviders()) {
                is Result.Success -> {
                    val providers = result.data.sortedBy { it.displayPriority }
                    _uiState.update {
                        it.copy(
                            streamingProviders = if (providers.isNotEmpty()) providers else FallbackProviders,
                            isProvidersLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            streamingProviders = FallbackProviders,
                            isProvidersLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenresLoading = true) }
            when (val result = filterRepository.fetchMovieGenres()) {
                is Result.Success -> {
                    val genres = result.data
                    _uiState.update {
                        it.copy(
                            availableGenres = if (genres.isNotEmpty()) genres else FallbackGenres,
                            isGenresLoading = false
                        )
                    }
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            availableGenres = FallbackGenres,
                            isGenresLoading = false
                        )
                    }
                }
            }
        }
    }

    fun toggleProvider(providerId: Int) {
        _uiState.update { current ->
            val updated = if (providerId in current.selectedProviderIds) {
                current.selectedProviderIds - providerId
            } else {
                current.selectedProviderIds + providerId
            }
            current.copy(selectedProviderIds = updated)
        }
    }

    fun selectAllPopularProviders() {
        _uiState.update { current ->
            val popularIds = current.streamingProviders.take(8).map { it.providerId }.toSet()
            current.copy(selectedProviderIds = popularIds)
        }
    }

    fun clearSelectedProviders() {
        _uiState.update { it.copy(selectedProviderIds = emptySet()) }
    }

    fun toggleGenre(genreId: Int) {
        _uiState.update { current ->
            val updated = if (genreId in current.selectedGenreIds) {
                current.selectedGenreIds - genreId
            } else {
                current.selectedGenreIds + genreId
            }
            current.copy(selectedGenreIds = updated)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningInWithGoogle = true, errorMessage = null) }
            val result = backupRepository.signInWithGoogle(activity = activity)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isSigningInWithGoogle = false,
                        googleUser = user
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSigningInWithGoogle = false,
                        errorMessage = if (error.isGoogleSignInCancelled()) null else error.message
                    )
                }
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoringBackup = true, errorMessage = null) }
            val result = backupRepository.restoreBackup()
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isRestoringBackup = false,
                        isBackupRestored = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRestoringBackup = false,
                        errorMessage = error.message
                    )
                }
            }
        }
    }

    fun skipRestoreBackup() {
        _uiState.update { it.copy(isBackupRestoreSkipped = true) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        val FallbackProviders = listOf(
            ProviderInfo(
                logoPath = "/9A1JSVmSxsya9Wi1ilxQjK5nmTr.jpg",
                providerId = 8,
                providerName = "Netflix",
                displayPriority = 0
            ),
            ProviderInfo(
                logoPath = "/emthp39XA2GUNqvrIIKy8a5J0v4.jpg",
                providerId = 9,
                providerName = "Amazon Prime Video",
                displayPriority = 1
            ),
            ProviderInfo(
                logoPath = "/7rwgEs15tFwyR9NPQ5vpzxTj19Q.jpg",
                providerId = 337,
                providerName = "Disney+",
                displayPriority = 2
            ),
            ProviderInfo(
                logoPath = "/2E03nojritSlmM2rwFL8CTEi580.jpg",
                providerId = 350,
                providerName = "Apple TV+",
                displayPriority = 3
            ),
            ProviderInfo(
                logoPath = "/fptnZJbLzKU50Pq8vYkP01DqgQ2.jpg",
                providerId = 1899,
                providerName = "Max",
                displayPriority = 4
            ),
            ProviderInfo(
                logoPath = "/zxrVdFjIjLqkfnwyghn2XuMinvl.jpg",
                providerId = 15,
                providerName = "Hulu",
                displayPriority = 5
            ),
            ProviderInfo(
                logoPath = "/xbhHLa3VGvKiEOWZvdG4rVvO8h7.jpg",
                providerId = 531,
                providerName = "Paramount+",
                displayPriority = 6
            ),
            ProviderInfo(
                logoPath = "/8VCV78eh0Rj76BPgtqwF245qORr.jpg",
                providerId = 386,
                providerName = "Peacock",
                displayPriority = 7
            )
        )

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
