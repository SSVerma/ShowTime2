package com.ssverma.feature.account.ui.backup

import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.ui.UiText

data class BackupSyncUiState(
    val googleUser: GoogleUser? = null,
    val isSigningIn: Boolean = false,
    val isSigningOut: Boolean = false,
    val backupStatus: BackupStatus = BackupStatus.Idle,
    val lastBackupMetadata: BackupMetadata? = null,
    val backupFrequency: BackupFrequency = BackupFrequency.OFF,
    val backupOverWifiOnly: Boolean = true,
    val isProActive: Boolean = false,
    val message: UiText? = null
)
