package com.ssverma.core.ui

import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure

sealed interface UiState<out S, out FeatureFailure> {
    object Idle : UiState<Nothing, Nothing>
    data class Success<out S>(val data: S) : UiState<S, Nothing>
    object Loading : UiState<Nothing, Nothing>
    data class Error<out FeatureFailure>(
        val failure: Failure<FeatureFailure>
    ) : UiState<Nothing, FeatureFailure>
}

fun <S, FeatureFailure> Result<S, Failure<FeatureFailure>>.asErrorOrSuccessUiState(): UiState<S, FeatureFailure> {
    return when (this) {
        is Result.Error -> {
            UiState.Error(this.error)
        }
        is Result.Success -> {
            UiState.Success(data = this.data)
        }
    }
}