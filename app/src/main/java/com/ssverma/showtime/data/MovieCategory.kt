package com.ssverma.showtime.data

import androidx.annotation.StringRes

data class MovieCategory(
    val id: Int,
    @StringRes val nameRes: Int
)