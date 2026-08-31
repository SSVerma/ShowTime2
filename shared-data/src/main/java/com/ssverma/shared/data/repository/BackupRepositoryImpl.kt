package com.ssverma.shared.data.repository

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.drive.GoogleDriveBackupClient
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.model.BackupSnapshot
import com.ssverma.shared.data.worker.PeriodicBackupWorker
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val googleAuthClient: GoogleAuthClient,
    private val googleDriveBackupClient: GoogleDriveBackupClient,
    private val favoriteDao: FavoriteDao,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val customListDao: CustomListDao,
    private val appConfigRepository: AppConfigRepository,
    keyValueStorageClient: KeyValueStorageClient
) : BackupRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson: Gson = GsonBuilder().create()

    private val backupSettingsStorage: KeyValueStorage =
        keyValueStorageClient.createKeyValueStorage(
            context = context,
            config = KeyValueStorageConfig(fileName = "backup_settings_prefs")
        )

    override val googleUser: StateFlow<GoogleUser?> = googleAuthClient.currentUser

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    override val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    private val _lastBackupMetadata = MutableStateFlow<BackupMetadata?>(null)
    override val lastBackupMetadata: StateFlow<BackupMetadata?> = _lastBackupMetadata.asStateFlow()

    private val _backupFrequency = MutableStateFlow(BackupFrequency.OFF)
    override val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency.asStateFlow()

    private val _backupOverWifiOnly = MutableStateFlow(true)
    override val backupOverWifiOnly: StateFlow<Boolean> = _backupOverWifiOnly.asStateFlow()

    init {
        scope.launch {
            loadBackupSettings()
        }
        scope.launch {
            loadExistingBackupMetadata()
        }
    }

    private suspend fun loadBackupSettings() {
        val (frequency, wifiOnly) = backupSettingsStorage.data.map { prefs ->
            val freqName = prefs[KEY_BACKUP_FREQUENCY]
            val freq = BackupFrequency.fromName(freqName)
            val wifi = prefs[KEY_BACKUP_OVER_WIFI] ?: true
            Pair(freq, wifi)
        }.first()

        _backupFrequency.value = frequency
        _backupOverWifiOnly.value = wifiOnly

        if (frequency.isAutomated) {
            PeriodicBackupWorker.schedule(
                context = context,
                intervalDays = frequency.intervalDays,
                wifiOnly = wifiOnly
            )
        }
    }

    override suspend fun setBackupFrequency(frequency: BackupFrequency) {
        backupSettingsStorage.edit { prefs ->
            prefs[KEY_BACKUP_FREQUENCY] = frequency.name
        }
        _backupFrequency.value = frequency

        if (frequency.isAutomated) {
            PeriodicBackupWorker.schedule(
                context = context,
                intervalDays = frequency.intervalDays,
                wifiOnly = _backupOverWifiOnly.value
            )
        } else {
            PeriodicBackupWorker.cancel(context)
        }
    }

    override suspend fun setBackupOverWifiOnly(wifiOnly: Boolean) {
        backupSettingsStorage.edit { prefs ->
            prefs[KEY_BACKUP_OVER_WIFI] = wifiOnly
        }
        _backupOverWifiOnly.value = wifiOnly

        if (_backupFrequency.value.isAutomated) {
            PeriodicBackupWorker.schedule(
                context = context,
                intervalDays = _backupFrequency.value.intervalDays,
                wifiOnly = wifiOnly
            )
        }
    }

    private suspend fun loadExistingBackupMetadata() = withContext(Dispatchers.IO) {
        val json =
            googleDriveBackupClient.readCompressedBackup(BACKUP_FILE_NAME) ?: return@withContext
        try {
            val snapshot = gson.fromJson(json, BackupSnapshot::class.java)
            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = json,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = snapshot.favorites.size,
                watchlistCount = snapshot.watchlist.size,
                historyCount = snapshot.history.size,
                customListsCount = snapshot.customLists.size,
                customListItemsCount = snapshot.customListItems.size
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

    override suspend fun getEffectiveUserId(): String {
        return googleAuthClient.getEffectiveUserId()
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
            val customLists = customListDao.getAllLists()
            val customListItems = customListDao.getAllListItems()

            val currentTheme = appConfigRepository.appTheme.firstOrNull() ?: AppTheme.System
            val currentRegion = appConfigRepository.watchProviderRegion.value

            val preferences = mapOf(
                KEY_PREF_THEME to currentTheme.name,
                KEY_PREF_REGION to currentRegion
            )

            _backupStatus.value = BackupStatus.InProgress(
                operation = BackupOperation.BACKUP,
                progressPercent = 50
            )

            val snapshot = BackupSnapshot(
                version = 2,
                timestamp = System.currentTimeMillis(),
                favorites = favorites,
                watchlist = watchlist,
                history = history,
                customLists = customLists,
                customListItems = customListItems,
                preferences = preferences
            )

            val jsonPayload = gson.toJson(snapshot)
            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = jsonPayload,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = favorites.size,
                watchlistCount = watchlist.size,
                historyCount = history.size,
                customListsCount = customLists.size,
                customListItemsCount = customListItems.size
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
            if (snapshot.customLists.isNotEmpty()) {
                customListDao.insertAllLists(snapshot.customLists)
            }
            if (snapshot.customListItems.isNotEmpty()) {
                customListDao.insertAllListItems(snapshot.customListItems)
            }

            // Restore preferences if available
            snapshot.preferences[KEY_PREF_THEME]?.let { themeName ->
                val theme = AppTheme.fromName(themeName)
                appConfigRepository.updateAppTheme(theme)
            }
            snapshot.preferences[KEY_PREF_REGION]?.let { regionCode ->
                if (regionCode.isNotBlank()) {
                    appConfigRepository.updateWatchProviderRegion(regionCode)
                }
            }

            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = json,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = snapshot.favorites.size,
                watchlistCount = snapshot.watchlist.size,
                historyCount = snapshot.history.size,
                customListsCount = snapshot.customLists.size,
                customListItemsCount = snapshot.customListItems.size
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
        private val KEY_BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        private val KEY_BACKUP_OVER_WIFI = booleanPreferencesKey("backup_over_wifi")
        private const val KEY_PREF_THEME = "pref_theme"
        private const val KEY_PREF_REGION = "pref_region"
    }
}
