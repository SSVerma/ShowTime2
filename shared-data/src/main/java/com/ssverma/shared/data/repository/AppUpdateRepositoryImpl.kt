package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.model.AppUpdateStatus
import com.ssverma.shared.domain.repository.AppUpdateRepository
import com.ssverma.shared.domain.utils.AppConfigConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfigProvider: AppConfigProvider,
    keyValueStorageClient: KeyValueStorageClient
) : AppUpdateRepository {

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = PREFS_FILE_NAME)
    )

    override fun observeAppUpdateStatus(currentVersionCode: Long): Flow<AppUpdateStatus> {
        val remoteConfigFlow = combine(
            appConfigProvider.observeLong(
                key = AppConfigConstants.KEY_MIN_SUPPORTED_VERSION_CODE,
                defaultValue = 0L
            ),
            appConfigProvider.observeLong(
                key = AppConfigConstants.KEY_LATEST_VERSION_CODE,
                defaultValue = 0L
            ),
            appConfigProvider.observeString(
                key = AppConfigConstants.KEY_UPDATE_TITLE,
                defaultValue = ""
            ),
            appConfigProvider.observeString(
                key = AppConfigConstants.KEY_UPDATE_MESSAGE,
                defaultValue = ""
            )
        ) { minSupportedVersion, latestVersion, title, message ->
            RemoteConfigValues(minSupportedVersion, latestVersion, title, message)
        }

        val localPrefsFlow = combine(
            storage.observe(key = KEY_LAST_DISMISSED_SOFT_UPDATE_VERSION, default = 0L),
            storage.observe(key = KEY_LAST_DISMISSED_SOFT_UPDATE_TIMESTAMP, default = 0L)
        ) { lastDismissedVersion, lastDismissedTimestamp ->
            lastDismissedVersion to lastDismissedTimestamp
        }

        return combine(
            remoteConfigFlow,
            localPrefsFlow
        ) { remote, (lastDismissedVersion, lastDismissedTimestamp) ->
            calculateUpdateStatus(
                currentVersionCode = currentVersionCode,
                minSupportedVersion = remote.minSupportedVersion,
                latestVersion = remote.latestVersion,
                title = remote.title.takeIf { it.isNotBlank() },
                message = remote.message.takeIf { it.isNotBlank() },
                lastDismissedVersion = lastDismissedVersion,
                lastDismissedTimestamp = lastDismissedTimestamp
            )
        }
    }

    override suspend fun checkAppUpdateStatus(currentVersionCode: Long): AppUpdateStatus {
        val minSupportedVersion = appConfigProvider.getLong(
            key = AppConfigConstants.KEY_MIN_SUPPORTED_VERSION_CODE,
            defaultValue = 0L
        )
        val latestVersion = appConfigProvider.getLong(
            key = AppConfigConstants.KEY_LATEST_VERSION_CODE,
            defaultValue = 0L
        )
        val title = appConfigProvider.getString(
            key = AppConfigConstants.KEY_UPDATE_TITLE,
            defaultValue = ""
        )
        val message = appConfigProvider.getString(
            key = AppConfigConstants.KEY_UPDATE_MESSAGE,
            defaultValue = ""
        )
        val lastDismissedVersion = storage.read(
            key = KEY_LAST_DISMISSED_SOFT_UPDATE_VERSION,
            default = 0L
        )
        val lastDismissedTimestamp = storage.read(
            key = KEY_LAST_DISMISSED_SOFT_UPDATE_TIMESTAMP,
            default = 0L
        )

        return calculateUpdateStatus(
            currentVersionCode = currentVersionCode,
            minSupportedVersion = minSupportedVersion,
            latestVersion = latestVersion,
            title = title.takeIf { it.isNotBlank() },
            message = message.takeIf { it.isNotBlank() },
            lastDismissedVersion = lastDismissedVersion,
            lastDismissedTimestamp = lastDismissedTimestamp
        )
    }

    override suspend fun dismissSoftUpdate(versionCode: Long) {
        storage.write(key = KEY_LAST_DISMISSED_SOFT_UPDATE_VERSION, value = versionCode)
        storage.write(
            key = KEY_LAST_DISMISSED_SOFT_UPDATE_TIMESTAMP,
            value = System.currentTimeMillis()
        )
    }

    override suspend fun isSoftUpdateDismissed(versionCode: Long): Boolean {
        val lastDismissedVersion = storage.read(
            key = KEY_LAST_DISMISSED_SOFT_UPDATE_VERSION,
            default = 0L
        )
        val lastDismissedTimestamp = storage.read(
            key = KEY_LAST_DISMISSED_SOFT_UPDATE_TIMESTAMP,
            default = 0L
        )

        if (lastDismissedVersion < versionCode) return false
        val daysElapsed = (System.currentTimeMillis() - lastDismissedTimestamp) / MILLIS_PER_DAY
        return daysElapsed < SOFT_UPDATE_COOLDOWN_DAYS
    }

    private fun calculateUpdateStatus(
        currentVersionCode: Long,
        minSupportedVersion: Long,
        latestVersion: Long,
        title: String?,
        message: String?,
        lastDismissedVersion: Long,
        lastDismissedTimestamp: Long
    ): AppUpdateStatus {
        return when {
            minSupportedVersion > currentVersionCode -> {
                AppUpdateStatus.ForceUpdate(
                    currentVersionCode = currentVersionCode,
                    minSupportedVersionCode = minSupportedVersion,
                    title = title,
                    message = message
                )
            }

            latestVersion > currentVersionCode -> {
                val shouldPrompt = when {
                    // 1. A brand new version release that was never dismissed before
                    latestVersion > lastDismissedVersion -> true

                    // 2. Same version previously dismissed: check if the 7-day cooldown expired
                    latestVersion == lastDismissedVersion -> {
                        val daysElapsed =
                            (System.currentTimeMillis() - lastDismissedTimestamp) / MILLIS_PER_DAY
                        daysElapsed >= SOFT_UPDATE_COOLDOWN_DAYS
                    }

                    else -> false
                }

                if (shouldPrompt) {
                    AppUpdateStatus.SoftUpdate(
                        currentVersionCode = currentVersionCode,
                        latestVersionCode = latestVersion,
                        title = title,
                        message = message
                    )
                } else {
                    AppUpdateStatus.UpToDate
                }
            }

            else -> AppUpdateStatus.UpToDate
        }
    }

    private data class RemoteConfigValues(
        val minSupportedVersion: Long,
        val latestVersion: Long,
        val title: String,
        val message: String
    )

    companion object {
        private const val PREFS_FILE_NAME = "showtime_app_update_prefs"
        const val SOFT_UPDATE_COOLDOWN_DAYS = 7L
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L

        val KEY_LAST_DISMISSED_SOFT_UPDATE_VERSION =
            longPreferencesKey("last_dismissed_soft_update_version")
        val KEY_LAST_DISMISSED_SOFT_UPDATE_TIMESTAMP =
            longPreferencesKey("last_dismissed_soft_update_timestamp")
    }
}
