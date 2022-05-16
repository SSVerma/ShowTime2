package com.ssverma.core.domain.failure

sealed interface Failure<out FeatureFailure> {
    data class FeatureFailure<out FeatureFailure>(
        val featureFailureType: FeatureFailure
    ) : Failure<FeatureFailure>

    sealed interface CoreFailure : Failure<Nothing> {
        object NetworkFailure : CoreFailure
        object ServiceFailure : CoreFailure
        object UnexpectedFailure : CoreFailure
    }
}