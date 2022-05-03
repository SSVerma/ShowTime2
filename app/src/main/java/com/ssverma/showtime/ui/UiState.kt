package com.ssverma.showtime.ui

import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure

sealed interface FetchDataUiState<out S, out FeatureFailure> {
    object Idle : FetchDataUiState<Nothing, Nothing>
    data class Success<out S>(val data: S) : FetchDataUiState<S, Nothing>
    object Loading : FetchDataUiState<Nothing, Nothing>
    data class Error<out FeatureFailure>(
        val failure: Failure<FeatureFailure>
    ) : FetchDataUiState<Nothing, FeatureFailure>
}

fun <S, FeatureFailure> DomainResult<S, Failure<FeatureFailure>>.asFetchDataUiState(): FetchDataUiState<S, FeatureFailure> {
    return when (this) {
        is DomainResult.Error -> {
            FetchDataUiState.Error(this.error)
        }
        is DomainResult.Success -> {
            FetchDataUiState.Success(data = this.data)
        }
    }
}