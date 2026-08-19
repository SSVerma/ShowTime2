package com.ssverma.shared.data.repository

import android.app.Activity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.drive.GoogleDriveBackupClient
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.model.BackupSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val googleDriveBackupClient: GoogleDriveBackupClient,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao
) : BackupRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson: Gson = GsonBuilder().create()

    override val googleUser: StateFlow<GoogleUser?> = googleAuthClient.currentUser

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    override val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _lastBackupMetadata = MutableStateFlow<BackupMetadata?>(null)
    override val lastBackupMetadata: StateFlow<BackupMetadata?> = _lastBackupMetadata.asStateFlow()

    init {
        scope.launch {
            loadExistingBackupMetadata()
        }
    }

    private suspend fun loadExistingBackupMetadata() = withContext(Dispatchers.IO) {
        val json =
            googleDriveBackupClient.readCompressedBackup(BACKUP_FILE_NAME) ?: return@withContext
        try {
            val snapshot = gson.fromJson(json, BackupSnapshot::class.java)
            val file = googleDriveBackupClient.getBackupFile(BACKUP_FILE_NAME)
            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = json,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = snapshot.favorites.size,
                watchlistCount = snapshot.watchlist.size,
                historyCount = snapshot.history.size
            )
            _lastBackupMetadata.value = metadata
        } catch (e: Exception) {
            // Ignore corrupted cache
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): Result<GoogleUser> {
        return googleAuthClient.signIn(activity)
    }

    override suspend fun signOutGoogle() {
        googleAuthClient.signOut()
    }

    override suspend fun backupNow(): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        _backupStatus.value = BackupStatus.InProgress(
            operation = BackupOperation.BACKUP,
            progressPercent = 10
        )

        try {
            val favorites = favoriteDao.getAllFavorites()
            val watchlist = watchlistDao.getAllWatchlist()
            val history = watchHistoryDao.getAllHistory()

            _backupStatus.value = BackupStatus.InProgress(
                operation = BackupOperation.BACKUP,
                progressPercent = 50
            )

            val snapshot = BackupSnapshot(
                version = 1,
                timestamp = System.currentTimeMillis(),
                favorites = favorites,
                watchlist = watchlist,
                history = history,
                preferences = emptyMap()
            )

            val jsonPayload = gson.toJson(snapshot)
            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = jsonPayload,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = favorites.size,
                watchlistCount = watchlist.size,
                historyCount = history.size
            )

            _lastBackupMetadata.value = metadata
            _backupStatus.value = BackupStatus.Success(
                operation = BackupOperation.BACKUP,
                metadata = metadata
            )

            Result.success(metadata)
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(
                operation = BackupOperation.BACKUP,
                message = e.localizedMessage ?: "Backup failed"
            )
            Result.failure(e)
        }
    }

    override suspend fun restoreBackup(): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        _backupStatus.value = BackupStatus.InProgress(
            operation = BackupOperation.RESTORE,
            progressPercent = 20
        )

        try {
            val json = googleDriveBackupClient.readCompressedBackup(BACKUP_FILE_NAME)
            if (json.isNullOrBlank()) {
                val error = IllegalStateException("No backup found to restore")
                _backupStatus.value = BackupStatus.Error(
                    operation = BackupOperation.RESTORE,
                    message = "No backup found in cloud storage"
                )
                return@withContext Result.failure(error)
            }

            val snapshot = gson.fromJson(json, BackupSnapshot::class.java)

            _backupStatus.value = BackupStatus.InProgress(
                operation = BackupOperation.RESTORE,
                progressPercent = 60
            )

            if (snapshot.favorites.isNotEmpty()) {
                favoriteDao.insertAll(snapshot.favorites)
            }
            if (snapshot.watchlist.isNotEmpty()) {
                watchlistDao.insertAll(snapshot.watchlist)
            }
            if (snapshot.history.isNotEmpty()) {
                watchHistoryDao.insertAll(snapshot.history)
            }

            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = json,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = snapshot.favorites.size,
                watchlistCount = snapshot.watchlist.size,
                historyCount = snapshot.history.size
            )

            _lastBackupMetadata.value = metadata
            _backupStatus.value = BackupStatus.Success(
                operation = BackupOperation.RESTORE,
                metadata = metadata
            )

            Result.success(metadata)
        } catch (e: Exception) {
            _backupStatus.value = BackupStatus.Error(
                operation = BackupOperation.RESTORE,
                message = e.localizedMessage ?: "Restore failed"
            )
            Result.failure(e)
        }
    }

    override fun resetStatus() {
        _backupStatus.value = BackupStatus.Idle
    }

    companion object {
        private const val BACKUP_FILE_NAME = "showtime_backup.json.gz"
    }
}
