package com.ssverma.shared.domain.utils

import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle

object DateUtils {
    fun currentDate(): LocalDate {
        return LocalDate.now()
    }

    fun parseIsoDate(date: String?): LocalDate? {
        return try {
            date?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            null
        }
    }

    fun parseIsoDateTime(dateTime: String?): LocalDateTime? {
        return try {
            dateTime?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            null
        }
    }

    fun parseIsoOffsetDateTime(dateTime: String?): OffsetDateTime? {
        return try {
            dateTime?.let { OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
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

    fun formatDateTime(
        dateTime: LocalDateTime,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    ): String? {
        return try {
            dateTime.format(formatter)
        } catch (e: DateTimeException) {
            e.printStackTrace()
            null
        }
    }

    fun formatDateTime(
        dateTime: OffsetDateTime,
        formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    ): String? {
        return try {
            dateTime.format(formatter)
        } catch (e: DateTimeException) {
            e.printStackTrace()
            null
        }
    }

    fun formatMinutes(minutes: Int): String {
        val hours: Int = minutes / 60
        val remainingMinutes: Int = minutes % 60

        return String.format("%dh : %02dm", hours, remainingMinutes)
    }
}

fun LocalDate.formatAsIso(): String? {
    return DateUtils.formatDate(this, DateTimeFormatter.ISO_LOCAL_DATE)
}

fun LocalDateTime.formatAsIso(): String? {
    return DateUtils.formatDateTime(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
}

fun OffsetDateTime.formatAsIso(): String? {
    return DateUtils.formatDateTime(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}

fun LocalDate.formatLocally(formatStyle: FormatStyle = FormatStyle.MEDIUM): String? {
    return DateUtils.formatDate(this, DateTimeFormatter.ofLocalizedDate(formatStyle))
}

fun LocalDateTime.formatLocally(formatStyle: FormatStyle = FormatStyle.MEDIUM): String? {
    return DateUtils.formatDateTime(this, DateTimeFormatter.ofLocalizedDateTime(formatStyle))
}

fun OffsetDateTime.formatLocally(formatStyle: FormatStyle = FormatStyle.MEDIUM): String? {
    return DateUtils.formatDateTime(this, DateTimeFormatter.ofLocalizedDateTime(formatStyle))
}