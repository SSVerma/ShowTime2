package com.ssverma.shared.analytics.backup

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class BackupAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class BackupStarted(
        val isAutomated: Boolean,
        val sourceScreen: String
    ) : BackupAnalyticsEvent(
        eventName = BackupAnalyticsEventName.CLOUD_BACKUP_STARTED,
        params = mapOf(
            BackupAnalyticsKeys.IS_AUTOMATED to isAutomated,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class BackupCompleted(
        val success: Boolean,
        val isAutomated: Boolean,
        val itemCount: Int? = null,
        val sourceScreen: String
    ) : BackupAnalyticsEvent(
        eventName = BackupAnalyticsEventName.CLOUD_BACKUP_COMPLETED,
        params = mutableMapOf<String, AnalyticsParam>().apply {
            putAll(
                listOfNotNull(
                    SharedAnalyticsKeys.SUCCESS to success,
                    BackupAnalyticsKeys.IS_AUTOMATED to isAutomated,
                    itemCount?.let { SharedAnalyticsKeys.ITEM_COUNT to it },
                    SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
                )
            )
        }
    )

    data class RestoreStarted(
        val sourceScreen: String
    ) : BackupAnalyticsEvent(
        eventName = BackupAnalyticsEventName.CLOUD_RESTORE_STARTED,
        params = mapOf(
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class RestoreCompleted(
        val success: Boolean,
        val sourceScreen: String
    ) : BackupAnalyticsEvent(
        eventName = BackupAnalyticsEventName.CLOUD_RESTORE_COMPLETED,
        params = mapOf(
            SharedAnalyticsKeys.SUCCESS to success,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class FrequencyChanged(
        val frequency: String,
        val sourceScreen: String
    ) : BackupAnalyticsEvent(
        eventName = BackupAnalyticsEventName.BACKUP_FREQUENCY_CHANGED,
        params = mapOf(
            BackupAnalyticsKeys.FREQUENCY to frequency,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
