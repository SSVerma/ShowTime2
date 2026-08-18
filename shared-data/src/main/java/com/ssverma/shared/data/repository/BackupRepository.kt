package com.ssverma.shared.data.repository

import android.app.Activity
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import kotlinx.coroutines.flow.StateFlow

interface BackupRepository {
    val googleUser: StateFlow<GoogleUser?>
    val backupStatus: StateFlow<BackupStatus>
    val lastBackupMetadata: StateFlow<BackupMetadata?>

    suspend fun signInWithGoogle(activity: Activity): Result<GoogleUser>
    suspend fun signOutGoogle()
    suspend fun backupNow(): Result<BackupMetadata>
    suspend fun restoreBackup(): Result<BackupMetadata>
    fun resetStatus()
}
