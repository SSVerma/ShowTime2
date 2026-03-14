package com.ssverma.shared.domain.utils

import java.text.DecimalFormat
import java.util.Locale

object FormatterUtils {

    fun toRangeSymbol(value: Float): String {
        return when {
            value > 1000000 -> "1M+"
            value > 100000 -> "1L+"
            value > 10000 -> "10K+"
            value > 1000 -> {
                val result = roundOffDecimal((value / 1000).toDouble())
                "${result}K+"
            }
            else -> value.toInt().toString()
        }
    }

    fun roundOffDecimal(number: Double, pattern: String = "#.#"): Double {
        val df = DecimalFormat(pattern)
        return df.format(number).toDoubleOrNull() ?: 0f.toDouble()
    }

    fun formatRating(voteAvgPercentage: Float): String {
        return String.format(Locale.getDefault(), "%.1f", voteAvgPercentage / 10f)
    }

    fun formatVoteCount(voteCount: Int): String {
        return "($voteCount)"
    }
}

fun formatPopularity(popularity: Float): String {
    val popInt = popularity.toInt()
    return when {
        popInt >= 1_000_000 -> "${popInt / 1_000_000}M+"
        popInt >= 1_000 -> "${popInt / 1_000}K+"
        popInt >= 100 -> "${(popInt / 100) * 100}+" // Rounds 511 down to 500+
        else -> popInt.toString()
    }
}
