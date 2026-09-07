package com.ssverma.shared.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ssverma.core.notifications.ShowTimeNotificationManager

class AiringReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mediaId = inputData.getInt(KEY_MEDIA_ID, -1)
        val mediaType = inputData.getString(KEY_MEDIA_TYPE).orEmpty()
        val mediaTitle = inputData.getString(KEY_MEDIA_TITLE).orEmpty()
        val posterUrl = inputData.getString(KEY_POSTER_URL)
        val seasonNumber = if (inputData.keyValueMap.containsKey(KEY_SEASON_NUMBER)) {
            inputData.getInt(KEY_SEASON_NUMBER, 0)
        } else {
            null
        }
        val episodeNumber = if (inputData.keyValueMap.containsKey(KEY_EPISODE_NUMBER)) {
            inputData.getInt(KEY_EPISODE_NUMBER, 0)
        } else {
            null
        }
        val episodeTitle = inputData.getString(KEY_EPISODE_TITLE)
        val providerName = inputData.getString(KEY_PROVIDER_NAME)

        if (mediaId == -1 || mediaTitle.isBlank()) {
            return Result.failure()
        }

        val notificationTitle = buildNotificationTitle(
            mediaType = mediaType,
            mediaTitle = mediaTitle,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )

        val notificationMessage = buildNotificationMessage(
            mediaType = mediaType,
            episodeTitle = episodeTitle,
            providerName = providerName
        )

        val deepLink = buildDeepLink(mediaType, mediaId)

        val notificationManager = ShowTimeNotificationManager(applicationContext)
        notificationManager.showReminderNotification(
            title = notificationTitle,
            message = notificationMessage,
            imageUrl = posterUrl,
            deepLink = deepLink
        )

        return Result.success()
    }

    companion object {
        const val KEY_MEDIA_ID = "media_id"
        const val KEY_MEDIA_TYPE = "media_type"
        const val KEY_MEDIA_TITLE = "media_title"
        const val KEY_POSTER_URL = "poster_url"
        const val KEY_SEASON_NUMBER = "season_number"
        const val KEY_EPISODE_NUMBER = "episode_number"
        const val KEY_EPISODE_TITLE = "episode_title"
        const val KEY_PROVIDER_NAME = "provider_name"

        fun buildNotificationTitle(
            mediaType: String,
            mediaTitle: String,
            seasonNumber: Int?,
            episodeNumber: Int?
        ): String {
            return when (mediaType) {
                "tv" -> {
                    val epLabel = if (seasonNumber != null && episodeNumber != null) {
                        " S${seasonNumber}E${episodeNumber}"
                    } else {
                        ""
                    }
                    "$mediaTitle$epLabel airs today!"
                }

                else -> "$mediaTitle releases today!"
            }
        }

        fun buildNotificationMessage(
            mediaType: String,
            episodeTitle: String?,
            providerName: String?
        ): String {
            val parts = mutableListOf<String>()

            if (mediaType == "tv" && !episodeTitle.isNullOrBlank()) {
                parts.add("\"$episodeTitle\"")
            }

            if (!providerName.isNullOrBlank()) {
                parts.add("Available on $providerName")
            }

            if (parts.isEmpty()) {
                return when (mediaType) {
                    "tv" -> "A new episode is airing today. Don't miss it!"
                    else -> "Now available in cinemas. Don't miss it!"
                }
            }

            return parts.joinToString(" · ")
        }

        fun buildDeepLink(mediaType: String, mediaId: Int): String {
            val pathSegment = when (mediaType) {
                "tv" -> "tv"
                else -> "movie"
            }
            return "showtime://showtime.ssverma.in/$pathSegment/$mediaId"
        }
    }
}
