package com.ssverma.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.failure.Failure

@Composable
fun <S, FeatureFailure> StatefulContent(
    state: UiState<S, FeatureFailure>,
    modifier: Modifier = Modifier,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator(modifier) },
    onRetry: () -> Unit = {},
    coreErrorContent: @Composable (error: Failure.CoreFailure) -> Unit = {
        DefaultCoreErrorIndicator(failure = it, onRetry = onRetry, modifier = modifier)
    },
    featureErrorContent: @Composable (error: Failure.FeatureFailure<FeatureFailure>) -> Unit = {},
    idleContent: @Composable () -> Unit = {},
    content: @Composable (data: S) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "StatefulContentTransition",
        modifier = modifier
    ) { targetState ->
        when (targetState) {
            is UiState.Idle -> idleContent()
            is UiState.Loading -> loading()
            is UiState.Success -> content(targetState.data)
            is UiState.Error -> {
                when (val errorResult = targetState.failure) {
                    Failure.CoreFailure.NetworkFailure,
                    Failure.CoreFailure.ServiceFailure,
                    Failure.CoreFailure.UnexpectedFailure -> {
                        coreErrorContent(errorResult as Failure.CoreFailure)
                    }

                    is Failure.FeatureFailure -> {
                        featureErrorContent(errorResult)
                    }
                }
            }
        }
    }
}
