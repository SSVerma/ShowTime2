package com.ssverma.shared.domain.model.reminder

import com.ssverma.shared.domain.model.MediaType

enum class ReminderType {
    TV_EPISODE,
    MOVIE_RELEASE
}

data class AiringReminder(
    val id: Long = 0L,
    val mediaId: Int,
    val mediaType: MediaType,
    val reminderType: ReminderType,
    val mediaTitle: String,
    val posterImageUrl: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val airDate: String,
    val reminderTimeMillis: Long,
    val providerName: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayLabel: String
        get() = when (reminderType) {
            ReminderType.TV_EPISODE -> {
                val epLabel = if (seasonNumber != null && episodeNumber != null) {
                    "S${seasonNumber}E${episodeNumber}"
                } else {
                    ""
                }
                if (epLabel.isNotEmpty()) "$mediaTitle $epLabel" else mediaTitle
            }

            ReminderType.MOVIE_RELEASE -> mediaTitle
        }
}
