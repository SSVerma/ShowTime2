package com.ssverma.core.domain.model

sealed interface Gender {
    object Male : Gender
    object Female : Gender
    object Unknown : Gender
}