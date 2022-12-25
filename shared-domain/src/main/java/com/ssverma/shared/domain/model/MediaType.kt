package com.ssverma.shared.domain.model

sealed interface MediaType {
    object Movie : MediaType
    object Tv : MediaType
    object Person : MediaType
    object Unknown : MediaType
}