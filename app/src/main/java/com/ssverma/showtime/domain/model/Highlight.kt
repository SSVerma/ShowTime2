package com.ssverma.showtime.domain.model

import androidx.annotation.StringRes
import com.ssverma.showtime.R
import com.ssverma.showtime.utils.DateUtils

data class Highlight(
    @StringRes val labelRes: Int,
    val value: String
)

fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.toString()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate ?: "",
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.runtime,
            value = DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = "$$revenue"
        )
    )
}