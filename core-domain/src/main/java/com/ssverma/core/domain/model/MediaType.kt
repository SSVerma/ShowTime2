package com.ssverma.core.domain.model

sealed interface MediaType {
    object Movie : MediaType
    object Tv : MediaType
    object Unknown : MediaType
}