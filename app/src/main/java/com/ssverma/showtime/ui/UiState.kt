package com.ssverma.showtime.ui

import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure

sealed interface FetchDataUiState<out S, out F> {
    object Idle : FetchDataUiState<Nothing, Nothing>
    data class Success<out S>(val data: S) : FetchDataUiState<S, Nothing>
    object Loading : FetchDataUiState<Nothing, Nothing>
    data class Error<out F>(val failure: Failure<F>) : FetchDataUiState<Nothing, F>
}

fun <S, F> DomainResult<S, Failure<F>>.asFetchDataUiState(): FetchDataUiState<S, F> {
    return when (this) {
        is DomainResult.Error -> {
            FetchDataUiState.Error(this.error)
        }
        is DomainResult.Success -> {
            FetchDataUiState.Success(data = this.data)
        }
    }
}