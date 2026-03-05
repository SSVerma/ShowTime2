package com.ssverma.core.ui

import androidx.compose.runtime.Composable
import com.ssverma.shared.domain.failure.Failure

@Composable
fun <S, FeatureFailure> DriveCompose(
    uiState: UiState<S, FeatureFailure>,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    onRetry: () -> Unit = {},
    coreErrorContent: @Composable (error: Failure.CoreFailure) -> Unit = {
        DefaultCoreErrorIndicator(failure = it, onRetry = onRetry)
    },
    featureErrorContent: @Composable (error: Failure.FeatureFailure<FeatureFailure>) -> Unit = {},
    idleContent: @Composable () -> Unit = {},
    content: @Composable (data: S) -> Unit,
) {
    when (uiState) {
        is UiState.Idle -> {
            idleContent()
        }

        is UiState.Error -> {
            when (val errorResult = uiState.failure) {
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

        UiState.Loading -> {
            loading()
        }

        is UiState.Success -> {
            content(uiState.data)
        }
    }
}
