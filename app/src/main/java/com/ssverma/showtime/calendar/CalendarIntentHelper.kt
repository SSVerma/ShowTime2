package com.ssverma.showtime.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import java.util.TimeZone

data class CalendarEventData(
    val title: String,
    val description: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val timeZone: String
)

object CalendarIntentHelper {
    fun buildCalendarEventData(
        reminder: AiringReminder,
        defaultDurationMillis: Long = 3600000L
    ): CalendarEventData {
        val description = buildString {
            append(reminder.mediaTitle)
            if (reminder.reminderType == ReminderType.TV_EPISODE) {
                append(" - Season ${reminder.seasonNumber} Episode ${reminder.episodeNumber}")
                if (!reminder.episodeTitle.isNullOrBlank()) {
                    append(": \"${reminder.episodeTitle}\"")
                }
            }
            if (!reminder.providerName.isNullOrBlank()) {
                append("\nStreaming on: ${reminder.providerName}")
            }
            append("\n\nSet via ShowTime")
        }

        return CalendarEventData(
            title = reminder.displayLabel,
            description = description,
            startTimeMillis = reminder.reminderTimeMillis,
            endTimeMillis = reminder.reminderTimeMillis + defaultDurationMillis,
            timeZone = TimeZone.getDefault().id
        )
    }

    fun createCalendarInsertIntent(reminder: AiringReminder): Intent {
        val eventData = buildCalendarEventData(reminder)

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, eventData.title)
            putExtra(CalendarContract.Events.DESCRIPTION, eventData.description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventData.startTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eventData.endTimeMillis)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, eventData.timeZone)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun launchCalendarInsert(context: Context, reminder: AiringReminder): Boolean {
        val intent = createCalendarInsertIntent(reminder)
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
