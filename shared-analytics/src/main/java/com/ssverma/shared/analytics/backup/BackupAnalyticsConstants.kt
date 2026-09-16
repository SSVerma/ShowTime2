package com.ssverma.shared.analytics.backup

object BackupAnalyticsKeys {
    const val IS_AUTOMATED = "is_automated"
    const val FREQUENCY = "frequency"
}

object BackupAnalyticsEventName {
    const val CLOUD_BACKUP_STARTED = "cloud_backup_started"
    const val CLOUD_BACKUP_COMPLETED = "cloud_backup_completed"
    const val CLOUD_RESTORE_STARTED = "cloud_restore_started"
    const val CLOUD_RESTORE_COMPLETED = "cloud_restore_completed"
    const val BACKUP_FREQUENCY_CHANGED = "backup_frequency_changed"
}
