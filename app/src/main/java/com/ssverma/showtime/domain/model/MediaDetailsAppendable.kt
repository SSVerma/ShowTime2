package com.ssverma.showtime.domain.model

sealed interface MediaDetailsAppendable {
    object Keywords : MediaDetailsAppendable
    object Credits : MediaDetailsAppendable
    object Images : MediaDetailsAppendable
    object Videos : MediaDetailsAppendable
    object Lists : MediaDetailsAppendable
    object Reviews : MediaDetailsAppendable
    object Similar : MediaDetailsAppendable
    object Recommendations : MediaDetailsAppendable
}