package com.ssverma.showtime.domain.failure.person

sealed interface PersonFailure {
    object NotFound : PersonFailure
}