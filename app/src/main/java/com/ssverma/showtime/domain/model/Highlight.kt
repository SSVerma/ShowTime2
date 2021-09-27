package com.ssverma.showtime.domain.model

import androidx.annotation.StringRes

data class Highlight(
    @StringRes val labelRes: Int,
    val value: String
)