package com.ssverma.showtime.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.showtime.ui.onboarding.component.OnboardingBottomBar
import com.ssverma.showtime.ui.onboarding.component.OnboardingTopBar
import com.ssverma.showtime.ui.onboarding.step.OnboardingCloudStep
import com.ssverma.showtime.ui.onboarding.step.OnboardingSubscriptionsStep
import com.ssverma.showtime.ui.onboarding.step.OnboardingTasteStep
import com.ssverma.showtime.ui.onboarding.step.OnboardingWelcomeStep
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (Set<Int>, Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { OnboardingStep.values().size })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorMessage()
        }
    }

    val currentStep = OnboardingStep.values()[pagerState.currentPage]
    val syncedUiState = remember(uiState, currentStep) {
        uiState.copy(currentStep = currentStep)
    }

    BackHandler(enabled = !currentStep.isFirst) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    val finishOnboarding = {
        val seededGenres = if (uiState.selectedGenreIds.isNotEmpty()) {
            uiState.selectedGenreIds
        } else {
            setOf(28, 18, 878) // Default seed: Action, Drama, Sci-Fi
        }
        onCompleteOnboarding(uiState.selectedProviderIds, seededGenres)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            OnboardingTopBar(
                currentStep = currentStep,
                onBack = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }
            )

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (OnboardingStep.values()[page]) {
                    OnboardingStep.Welcome -> {
                        OnboardingWelcomeStep()
                    }

                    OnboardingStep.Subscriptions -> {
                        OnboardingSubscriptionsStep(
                            providers = uiState.streamingProviders,
                            selectedProviderIds = uiState.selectedProviderIds,
                            isLoading = uiState.isProvidersLoading,
                            onToggleProvider = { viewModel.toggleProvider(it) },
                            onSelectAllPopular = { viewModel.selectAllPopularProviders() },
                            onClearAll = { viewModel.clearSelectedProviders() }
                        )
                    }

                    OnboardingStep.Taste -> {
                        OnboardingTasteStep(
                            genres = uiState.availableGenres,
                            selectedGenreIds = uiState.selectedGenreIds,
                            isLoading = uiState.isGenresLoading,
                            onToggleGenre = { viewModel.toggleGenre(it) }
                        )
                    }

                    OnboardingStep.Cloud -> {
                        OnboardingCloudStep(
                            googleUser = uiState.googleUser,
                            isSigningIn = uiState.isSigningInWithGoogle,
                            onSignInWithGoogle = { viewModel.signInWithGoogle(it) }
                        )
                    }
                }
            }

            OnboardingBottomBar(
                uiState = syncedUiState,
                onNextStep = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onSkipStep = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onComplete = finishOnboarding
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
