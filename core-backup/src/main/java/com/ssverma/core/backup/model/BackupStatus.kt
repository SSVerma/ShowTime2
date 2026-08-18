package com.ssverma.core.backup.model

sealed interface BackupStatus {
    data object Idle : BackupStatus
    data class InProgress(val operation: BackupOperation, val progressPercent: Int = 0) : BackupStatus
    data class Success(val operation: BackupOperation, val metadata: BackupMetadata) : BackupStatus
    data class Error(val operation: BackupOperation, val message: String) : BackupStatus
}

enum class BackupOperation {
    BACKUP,
    RESTORE
}
