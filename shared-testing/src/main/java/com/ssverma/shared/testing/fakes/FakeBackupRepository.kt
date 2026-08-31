package com.ssverma.shared.testing.fakes

import android.app.Activity
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.shared.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBackupRepository : BackupRepository {

    private val _googleUser = MutableStateFlow<GoogleUser?>(null)
    override val googleUser: StateFlow<GoogleUser?> = _googleUser.asStateFlow()

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    override val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _lastBackupMetadata = MutableStateFlow<BackupMetadata?>(null)
    override val lastBackupMetadata: StateFlow<BackupMetadata?> = _lastBackupMetadata.asStateFlow()

    private val _backupFrequency = MutableStateFlow(BackupFrequency.OFF)
    override val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency.asStateFlow()

    private val _backupOverWifiOnly = MutableStateFlow(true)
    override val backupOverWifiOnly: StateFlow<Boolean> = _backupOverWifiOnly.asStateFlow()

    var shouldFailBackup: Boolean = false
    var shouldFailRestore: Boolean = false
    var signInFailureException: Throwable? = null

    fun setGoogleUser(user: GoogleUser?) {
        _googleUser.value = user
    }

    fun setBackupStatus(status: BackupStatus) {
        _backupStatus.value = status
    }

    fun setLastBackupMetadata(metadata: BackupMetadata?) {
        _lastBackupMetadata.value = metadata
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<GoogleUser> {
        signInFailureException?.let { return Result.failure(it) }
        val user = GoogleUser(
            email = "user@example.com",
            displayName = "Test User",
            photoUrl = "https://example.com/avatar.png",
            idToken = "fake_id_token"
        )
        _googleUser.value = user
        return Result.success(user)
    }

    override suspend fun signOutGoogle() {
        _googleUser.value = null
    }

    var fakeEffectiveUserId: String = "fake_user_123"

    override suspend fun getEffectiveUserId(): String {
        return fakeEffectiveUserId
    }

    override suspend fun backupNow(): Result<BackupMetadata> {
        if (shouldFailBackup) {
            val error = "Simulated backup failure"
            _backupStatus.value = BackupStatus.Error(
                operation = BackupOperation.BACKUP,
                message = error
            )
            return Result.failure(IllegalStateException(error))
        }

        val metadata = BackupMetadata(
            timestamp = System.currentTimeMillis(),
            formattedDate = "Just now",
            sizeBytes = 2048,
            formattedSize = "2.0 KB",
            deviceName = "Fake Test Device",
            favoritesCount = 10,
            watchlistCount = 5,
            historyCount = 20,
            customListsCount = 2,
            customListItemsCount = 8
        )
        _lastBackupMetadata.value = metadata
        _backupStatus.value = BackupStatus.Success(
            operation = BackupOperation.BACKUP,
            metadata = metadata
        )
        return Result.success(metadata)
    }

    override suspend fun restoreBackup(): Result<BackupMetadata> {
        if (shouldFailRestore) {
            val error = "Simulated restore failure"
            _backupStatus.value = BackupStatus.Error(
                operation = BackupOperation.RESTORE,
                message = error
            )
            return Result.failure(IllegalStateException(error))
        }

        val metadata = _lastBackupMetadata.value ?: BackupMetadata(
            timestamp = System.currentTimeMillis(),
            formattedDate = "Aug 18, 2026",
            sizeBytes = 1024,
            formattedSize = "1.0 KB",
            deviceName = "Cloud Backup Device",
            favoritesCount = 10,
            watchlistCount = 5,
            historyCount = 20,
            customListsCount = 2,
            customListItemsCount = 8
        )
        _backupStatus.value = BackupStatus.Success(
            operation = BackupOperation.RESTORE,
            metadata = metadata
        )
        return Result.success(metadata)
    }

    override suspend fun setBackupFrequency(frequency: BackupFrequency) {
        _backupFrequency.value = frequency
    }

    override suspend fun setBackupOverWifiOnly(wifiOnly: Boolean) {
        _backupOverWifiOnly.value = wifiOnly
    }

    override fun resetStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}
