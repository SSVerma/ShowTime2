package com.ssverma.showtime.utils

import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {
    fun currentDate(): LocalDate {
        return LocalDate.now()
    }

    fun parseIsoDate(date: String): LocalDate? {
        return try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            null
        }
    }

    fun formatDate(
        date: LocalDate,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    ): String? {
        return try {
            date.format(formatter)
        } catch (e: DateTimeException) {
            e.printStackTrace()
            null
        }
    }
}

fun LocalDate.formatAsIso(): String? {
    return DateUtils.formatDate(this, DateTimeFormatter.ISO_LOCAL_DATE)
}