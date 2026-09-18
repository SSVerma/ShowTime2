package com.ssverma.shared.domain.usecase.reminder

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.ReminderRepository
import javax.inject.Inject

class RemoveAiringReminderUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    suspend operator fun invoke(mediaId: Int, mediaType: MediaType) {
        reminderRepository.removeReminder(mediaId, mediaType)
    }
}
