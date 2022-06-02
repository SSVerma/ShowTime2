package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.core.domain.TimeWindow

fun TimeWindow.asTmdbQueryValue(): String {
    return when (this) {
        TimeWindow.Daily -> TmdbApiTiedConstants.AvailableTimeWindows.DAY
        TimeWindow.Weekly -> TmdbApiTiedConstants.AvailableTimeWindows.WEEK
    }
}