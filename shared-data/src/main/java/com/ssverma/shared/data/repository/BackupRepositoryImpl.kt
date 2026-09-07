package com.ssverma.shared.data.repository

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.model.BackupSnapshot
import com.ssverma.shared.data.worker.PeriodicBackupWorker
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.BacklogRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
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
    private val diaryDao: DiaryDao,
    private val showWatchProgressDao: ShowWatchProgressDao,
    private val episodeWatchHistoryDao: EpisodeWatchHistoryDao,
    private val backlogRepository: BacklogRepository,
    private val cinemaGameRepository: CinemaGameRepository,
    private val appConfigRepository: AppConfigRepository,
    private val firestore: FirebaseFirestore,
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
            googleUser.collectLatest { user ->
                if (user != null) {
                    fetchRemoteBackupMetadata()
                } else {
                    loadExistingBackupMetadata()
                }
            }
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

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix = if (isDebug) "dev_" else ""
    private val colUserBackups get() = "${colPrefix}user_backups"

    private fun compressGzip(raw: String): String {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzos ->
            gzos.write(raw.toByteArray(StandardCharsets.UTF_8))
        }
        val bytes = baos.toByteArray()
        return try {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    private fun decompressGzip(base64: String): String {
        val bytes = try {
            Base64.decode(base64, Base64.NO_WRAP)
        } catch (_: Throwable) {
            java.util.Base64.getDecoder().decode(base64)
        }
        return GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(StandardCharsets.UTF_8)
            .use {
                it.readText()
            }
    }

    private fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun loadExistingBackupMetadata() = withContext(Dispatchers.IO) {
        val json = googleDriveBackupClient.readCompressedBackup(BACKUP_FILE_NAME)
        if (!json.isNullOrBlank()) {
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
                    customListItemsCount = snapshot.customListItems.size,
                    diaryEntriesCount = snapshot.diaryEntries.size,
                    showProgressCount = snapshot.showProgress.size,
                    episodeHistoryCount = snapshot.episodeHistory.size,
                    challengesCount = snapshot.activeChallenges.size
                )
                _lastBackupMetadata.value = metadata
            } catch (_: Exception) {
                // Ignore corrupted cache
            }
        }
        fetchRemoteBackupMetadata()
    }

    override suspend fun fetchRemoteBackupMetadata(): Result<BackupMetadata?> =
        withContext(Dispatchers.IO) {
            try {
                val effectiveUid = getEffectiveUserId()
                val doc = firestore.collection(colUserBackups).document(effectiveUid).get().await()
                if (doc.exists()) {
                    val metadata = BackupMetadata(
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        formattedDate = doc.getString("formattedDate").orEmpty(),
                        sizeBytes = doc.getLong("sizeBytes") ?: 0L,
                        formattedSize = doc.getString("formattedSize").orEmpty(),
                        deviceName = doc.getString("deviceName").orEmpty(),
                        favoritesCount = doc.getLong("favoritesCount")?.toInt() ?: 0,
                        watchlistCount = doc.getLong("watchlistCount")?.toInt() ?: 0,
                        historyCount = doc.getLong("historyCount")?.toInt() ?: 0,
                        customListsCount = doc.getLong("customListsCount")?.toInt() ?: 0,
                        customListItemsCount = doc.getLong("customListItemsCount")?.toInt() ?: 0,
                        diaryEntriesCount = doc.getLong("diaryEntriesCount")?.toInt() ?: 0,
                        showProgressCount = doc.getLong("showProgressCount")?.toInt() ?: 0,
                        episodeHistoryCount = doc.getLong("episodeHistoryCount")?.toInt() ?: 0,
                        challengesCount = doc.getLong("challengesCount")?.toInt() ?: 0
                    )
                    _lastBackupMetadata.value = metadata

                    val remoteGzip = doc.getString("payloadGzip")
                    val remotePayload = when {
                        !remoteGzip.isNullOrBlank() -> try {
                            decompressGzip(remoteGzip)
                        } catch (_: Exception) {
                            null
                        }

                        else -> doc.getString("payloadJson")
                    }

                    if (!remotePayload.isNullOrBlank()) {
                        googleDriveBackupClient.saveCompressedBackup(
                            fileName = BACKUP_FILE_NAME,
                            jsonPayload = remotePayload,
                            timestamp = metadata.timestamp,
                            deviceName = metadata.deviceName,
                            favoritesCount = metadata.favoritesCount,
                            watchlistCount = metadata.watchlistCount,
                            historyCount = metadata.historyCount,
                            customListsCount = metadata.customListsCount,
                            customListItemsCount = metadata.customListItemsCount,
                            diaryEntriesCount = metadata.diaryEntriesCount,
                            showProgressCount = metadata.showProgressCount,
                            episodeHistoryCount = metadata.episodeHistoryCount,
                            challengesCount = metadata.challengesCount
                        )
                        doc.getString("payloadHash")?.let { hash ->
                            backupSettingsStorage.write(KEY_LAST_BACKUP_HASH, hash)
                        }
                    }
                    Result.success(metadata)
                } else {
                    Result.success(_lastBackupMetadata.value)
                }
            } catch (_: Exception) {
                Result.success(_lastBackupMetadata.value)
            }
        }

    override suspend fun signInWithGoogle(activity: Activity): Result<GoogleUser> {
        val result = googleAuthClient.signIn(activity)
        result.onSuccess {
            fetchRemoteBackupMetadata()
        }
        return result
    }

    override suspend fun signOutGoogle() {
        googleAuthClient.signOut()
        _lastBackupMetadata.value = null
        googleDriveBackupClient.deleteBackup(BACKUP_FILE_NAME)
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
            val diaryEntries = diaryDao.getAllDiaryEntriesList()
            val showProgress = showWatchProgressDao.getAllProgress()
            val episodeHistory = episodeWatchHistoryDao.getAllHistory()
            val activeChallenges = backlogRepository.activeChallengesFlow.first()
            val blindspots = backlogRepository.blindspotsFlow.first()
            val gameStats = cinemaGameRepository.getGameStats()

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
                diaryEntries = diaryEntries,
                showProgress = showProgress,
                episodeHistory = episodeHistory,
                activeChallenges = activeChallenges,
                blindspots = blindspots,
                gameStats = gameStats,
                preferences = preferences
            )

            val jsonPayload = gson.toJson(snapshot)
            val contentFingerprint = snapshot.copy(timestamp = 0L, deviceName = "")
            val payloadHash = computeSha256(gson.toJson(contentFingerprint))

            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = jsonPayload,
                timestamp = snapshot.timestamp,
                deviceName = snapshot.deviceName,
                favoritesCount = favorites.size,
                watchlistCount = watchlist.size,
                historyCount = history.size,
                customListsCount = customLists.size,
                customListItemsCount = customListItems.size,
                diaryEntriesCount = diaryEntries.size,
                showProgressCount = showProgress.size,
                episodeHistoryCount = episodeHistory.size,
                challengesCount = activeChallenges.size
            )

            // SHA-256 Checksum Guard: Skip Firestore write if local payload hash is identical to last upload
            val lastUploadedHash = backupSettingsStorage.read(KEY_LAST_BACKUP_HASH, "")
            val isHashUnchanged = lastUploadedHash.isNotBlank() && lastUploadedHash == payloadHash

            if (!isHashUnchanged) {
                // Cloud Firestore upload with in-memory GZIP compression
                try {
                    val effectiveUid = getEffectiveUserId()
                    val base64GzipPayload = compressGzip(jsonPayload)
                    val backupDoc = mapOf(
                        "uid" to effectiveUid,
                        "version" to snapshot.version,
                        "timestamp" to snapshot.timestamp,
                        "formattedDate" to metadata.formattedDate,
                        "deviceName" to metadata.deviceName,
                        "sizeBytes" to metadata.sizeBytes,
                        "formattedSize" to metadata.formattedSize,
                        "payloadHash" to payloadHash,
                        "isCompressed" to true,
                        "payloadGzip" to base64GzipPayload,
                        "favoritesCount" to metadata.favoritesCount,
                        "watchlistCount" to metadata.watchlistCount,
                        "historyCount" to metadata.historyCount,
                        "customListsCount" to metadata.customListsCount,
                        "customListItemsCount" to metadata.customListItemsCount,
                        "diaryEntriesCount" to metadata.diaryEntriesCount,
                        "showProgressCount" to metadata.showProgressCount,
                        "episodeHistoryCount" to metadata.episodeHistoryCount,
                        "challengesCount" to metadata.challengesCount
                    )
                    firestore.collection(colUserBackups).document(effectiveUid)
                        .set(backupDoc, SetOptions.merge()).await()
                    backupSettingsStorage.write(KEY_LAST_BACKUP_HASH, payloadHash)
                } catch (_: Exception) {
                    // Non-fatal if offline: local compressed backup was already saved
                }
            }

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
            var json: String? = null
            // Prioritize fetching latest from Cloud Firestore
            try {
                val effectiveUid = getEffectiveUserId()
                val doc =
                    firestore.collection(colUserBackups).document(effectiveUid).get().await()
                if (doc.exists()) {
                    val remoteGzip = doc.getString("payloadGzip")
                    json = when {
                        !remoteGzip.isNullOrBlank() -> try {
                            decompressGzip(remoteGzip)
                        } catch (_: Exception) {
                            null
                        }

                        else -> doc.getString("payloadJson")
                    }
                }
            } catch (_: Exception) {
                // Fallback to local storage if offline
            }

            if (json.isNullOrBlank()) {
                json = googleDriveBackupClient.readCompressedBackup(BACKUP_FILE_NAME)
            }

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
            if (snapshot.diaryEntries.isNotEmpty()) {
                diaryDao.insertAll(snapshot.diaryEntries)
            }
            if (snapshot.showProgress.isNotEmpty()) {
                showWatchProgressDao.insertAll(snapshot.showProgress)
            }
            if (snapshot.episodeHistory.isNotEmpty()) {
                episodeWatchHistoryDao.insertAll(snapshot.episodeHistory)
            }
            if (snapshot.activeChallenges.isNotEmpty() || snapshot.blindspots.isNotEmpty()) {
                backlogRepository.restoreBacklog(snapshot.activeChallenges, snapshot.blindspots)
            }
            snapshot.gameStats?.let { stats ->
                cinemaGameRepository.restoreGameStats(stats)
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
                customListItemsCount = snapshot.customListItems.size,
                diaryEntriesCount = snapshot.diaryEntries.size,
                showProgressCount = snapshot.showProgress.size,
                episodeHistoryCount = snapshot.episodeHistory.size,
                challengesCount = snapshot.activeChallenges.size
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
        private val KEY_LAST_BACKUP_HASH = stringPreferencesKey("backup_last_payload_hash")
        private const val KEY_PREF_THEME = "pref_theme"
        private const val KEY_PREF_REGION = "pref_region"
    }
}
