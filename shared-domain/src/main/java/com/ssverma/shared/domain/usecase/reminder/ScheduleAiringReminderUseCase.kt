package com.ssverma.shared.domain.usecase.reminder

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.reminder.AiringReminder
import com.ssverma.shared.domain.model.reminder.ReminderType
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.ReminderQuotaManager
import com.ssverma.shared.domain.repository.ReminderRepository
import com.ssverma.shared.domain.utils.ReminderTimeCalculator
import com.ssverma.shared.domain.utils.formatLocally
import java.time.LocalDate
import javax.inject.Inject

sealed interface ScheduleReminderResult {
    data class Success(val reminder: AiringReminder) : ScheduleReminderResult
    data object QuotaExceeded : ScheduleReminderResult
    data object TimePassed : ScheduleReminderResult
    data object NoSchedule : ScheduleReminderResult
    data class Error(val message: String?) : ScheduleReminderResult
}

class ScheduleAiringReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderQuotaManager: ReminderQuotaManager,
    private val appConfigRepository: AppConfigRepository
) {
    suspend operator fun invoke(
        mediaId: Int,
        mediaType: MediaType,
        mediaTitle: String,
        posterImageUrl: String,
        targetAirDate: LocalDate?,
        leadDays: Int,
        hour: Int,
        minute: Int,
        isProActive: Boolean,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeTitle: String? = null,
        providerName: String? = null
    ): ScheduleReminderResult {
        if (targetAirDate == null) {
            return ScheduleReminderResult.NoSchedule
        }

        // Save user schedule preferences
        appConfigRepository.updateReminderLeadDays(leadDays)
        appConfigRepository.updateReminderNotificationTime(hour, minute)

        val activeCount = reminderRepository.getActiveReminderCount()
        val canSchedule = reminderQuotaManager.canScheduleReminder(activeCount, isProActive)
        if (!canSchedule) {
            return ScheduleReminderResult.QuotaExceeded
        }

        val reminderTime = ReminderTimeCalculator.calculateReminderTime(
            airDate = targetAirDate,
            hour = hour,
            minute = minute,
            leadDays = leadDays
        ) ?: return ScheduleReminderResult.TimePassed

        val airDateStr = targetAirDate.formatLocally() ?: targetAirDate.toString()
        val reminderType = if (mediaType == MediaType.Tv) {
            ReminderType.TV_EPISODE
        } else {
            ReminderType.MOVIE_RELEASE
        }

        val reminder = AiringReminder(
            mediaId = mediaId,
            mediaType = mediaType,
            reminderType = reminderType,
            mediaTitle = mediaTitle,
            posterImageUrl = posterImageUrl,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            airDate = airDateStr,
            reminderTimeMillis = reminderTime,
            providerName = providerName
        )

        reminderRepository.addReminder(reminder)
        return ScheduleReminderResult.Success(reminder)
    }
}
