package com.ssverma.showtime.domain.core

sealed interface Failure<out F> {
    data class FeatureFailure<out F>(
        val featureFailureType: F
    ) : Failure<F>

    sealed interface CoreFailure : Failure<Nothing> {
        object NetworkFailure : CoreFailure
        object ServiceFailure : CoreFailure
        object UnexpectedFailure : CoreFailure
    }
}