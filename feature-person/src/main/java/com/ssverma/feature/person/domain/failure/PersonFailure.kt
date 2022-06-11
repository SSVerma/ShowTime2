package com.ssverma.feature.person.domain.failure

sealed interface PersonFailure {
    object NotFound : PersonFailure
}