package com.ssverma.core.domain.utils

import java.text.DecimalFormat

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
        return df.format(number).toDouble()
    }
}