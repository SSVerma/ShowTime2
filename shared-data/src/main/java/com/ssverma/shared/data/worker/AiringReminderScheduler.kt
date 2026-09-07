package com.ssverma.shared.data.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiringReminderScheduler @Inject constructor() {

    fun schedule(
        context: Context,
        mediaId: Int,
        mediaType: String,
        mediaTitle: String,
        posterImageUrl: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        providerName: String?,
        delayMillis: Long
    ) {
        val inputData = Data.Builder()
            .putInt(AiringReminderWorker.KEY_MEDIA_ID, mediaId)
            .putString(AiringReminderWorker.KEY_MEDIA_TYPE, mediaType)
            .putString(AiringReminderWorker.KEY_MEDIA_TITLE, mediaTitle)
            .putString(AiringReminderWorker.KEY_POSTER_URL, posterImageUrl)
            .putString(AiringReminderWorker.KEY_PROVIDER_NAME, providerName.orEmpty())
            .apply {
                if (seasonNumber != null) putInt(
                    AiringReminderWorker.KEY_SEASON_NUMBER,
                    seasonNumber
                )
                if (episodeNumber != null) putInt(
                    AiringReminderWorker.KEY_EPISODE_NUMBER,
                    episodeNumber
                )
                if (episodeTitle != null) putString(
                    AiringReminderWorker.KEY_EPISODE_TITLE,
                    episodeTitle
                )
            }
            .build()

        val effectiveDelay = if (delayMillis > 0) delayMillis else 0L

        val workRequest = OneTimeWorkRequestBuilder<AiringReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(mediaId, mediaType),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancel(context: Context, mediaId: Int, mediaType: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(mediaId, mediaType))
    }

    private fun uniqueWorkName(mediaId: Int, mediaType: String): String {
        return "airing_reminder_${mediaType}_$mediaId"
    }
}
