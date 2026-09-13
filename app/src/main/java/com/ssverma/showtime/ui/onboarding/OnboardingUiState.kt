package com.ssverma.showtime.ui.onboarding

import androidx.compose.runtime.Immutable
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo

enum class OnboardingStep(val stepIndex: Int) {
    Welcome(0),
    Subscriptions(1),
    Taste(2),
    Cloud(3);

    val isFirst: Boolean get() = this == Welcome
    val isLast: Boolean get() = this == Cloud
}

@Immutable
data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val streamingProviders: List<ProviderInfo> = emptyList(),
    val isProvidersLoading: Boolean = false,
    val selectedProviderIds: Set<Int> = emptySet(),
    val availableGenres: List<Genre> = emptyList(),
    val isGenresLoading: Boolean = false,
    val selectedGenreIds: Set<Int> = emptySet(),
    val googleUser: GoogleUser? = null,
    val isSigningInWithGoogle: Boolean = false,
    val errorMessage: String? = null
) {
    val canProceedFromTaste: Boolean get() = selectedGenreIds.size >= 3
}
