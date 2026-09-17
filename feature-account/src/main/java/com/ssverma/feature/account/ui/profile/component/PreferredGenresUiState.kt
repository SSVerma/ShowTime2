package com.ssverma.feature.account.ui.profile.component

import androidx.compose.runtime.Immutable
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre

@Immutable
data class PreferredGenresUiState(
    val genresState: UiState<List<Genre>, Failure.CoreFailure> = UiState.Loading,
    val selectedGenreIds: Set<Int> = emptySet(),
    val isSaving: Boolean = false
) {
    val selectedCount: Int get() = selectedGenreIds.size
    val isThresholdMet: Boolean get() = selectedCount >= MIN_GENRES
    val isMaxReached: Boolean get() = selectedCount >= MAX_GENRES
    val canSave: Boolean get() = selectedCount in MIN_GENRES..MAX_GENRES

    companion object {
        const val MIN_GENRES = 3
        const val MAX_GENRES = 5
    }
}

sealed interface PreferredGenresUiEffect {
    data object GenresSaved : PreferredGenresUiEffect
}
