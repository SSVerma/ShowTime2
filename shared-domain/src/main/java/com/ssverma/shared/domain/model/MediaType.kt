package com.ssverma.shared.domain.model

sealed interface MediaType {
    data object Movie : MediaType
    data object Tv : MediaType
    data object Person : MediaType
    data object Unknown : MediaType
}