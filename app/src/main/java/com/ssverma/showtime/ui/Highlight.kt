package com.ssverma.showtime.ui

import androidx.annotation.StringRes
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.extension.emptyIfAbsent
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils

data class Highlight(
    @StringRes val labelRes: Int,
    val value: String
)

fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.emptyIfNull(),
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
            value = if (runtime == 0) runtime.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) revenue.emptyIfAbsent() else "$$revenue"
        )
    )
}