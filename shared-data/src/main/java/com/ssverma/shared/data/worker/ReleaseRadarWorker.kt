package com.ssverma.shared.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ssverma.api.service.tmdb.response.RemoteProviderInfo
import com.ssverma.shared.domain.model.release.ReleaseRadarConfig
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReleaseRadarWorkerEntryPoint {
    fun releaseRadarProcessor(): ReleaseRadarProcessor
}

class ReleaseRadarWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                ReleaseRadarWorkerEntryPoint::class.java
            )
            when (entryPoint.releaseRadarProcessor().executeRadar()) {
                ReleaseRadarResult.Success -> Result.success()
                ReleaseRadarResult.Retry -> Result.retry()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "periodic_smart_release_radar"
        const val MAX_STREAMING_CALLS_PER_DAY = ReleaseRadarConfig.MAX_STREAMING_CALLS_PER_DAY
        const val STREAMING_WINDOW_MIN_DAYS = ReleaseRadarConfig.STREAMING_WINDOW_MIN_DAYS
        const val STREAMING_WINDOW_MAX_DAYS = ReleaseRadarConfig.STREAMING_WINDOW_MAX_DAYS
        const val STREAMING_RECHECK_COOLDOWN_DAYS =
            ReleaseRadarConfig.STREAMING_RECHECK_COOLDOWN_DAYS

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<ReleaseRadarWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun buildMovieDeepLink(mediaId: Int): String {
            return ReleaseRadarProcessor.buildMovieDeepLink(mediaId)
        }

        fun filterMatchedProviders(
            availableProviders: List<RemoteProviderInfo>,
            userSubscriptions: Set<Int>
        ): List<RemoteProviderInfo> {
            return ReleaseRadarProcessor.filterMatchedProviders(
                availableProviders,
                userSubscriptions
            )
        }

        fun formatStreamingProviderNames(providers: List<RemoteProviderInfo>): String {
            return ReleaseRadarProcessor.formatStreamingProviderNames(providers)
        }
    }
}
