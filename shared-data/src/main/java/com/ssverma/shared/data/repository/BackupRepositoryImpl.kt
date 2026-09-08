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
import com.google.gson.JsonObject
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.contributor.BackupContributor
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
import com.ssverma.shared.data.worker.PeriodicBackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
    private val contributors: Set<@JvmSuppressWildcards BackupContributor>,
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
                val jsonObject = gson.fromJson(json, JsonObject::class.java)
                val timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis()
                val deviceName = jsonObject.get("deviceName")?.asString.orEmpty()
                val featureCounts = mutableMapOf<String, Int>()

                val countsObj = jsonObject.getAsJsonObject("featureCounts")
                if (countsObj != null) {
                    countsObj.entrySet().forEach { (k, v) ->
                        featureCounts[k] = v.asInt
                    }
                } else {
                    val legacyKeys = listOf(
                        BackupMetadata.KEY_FAVORITES,
                        BackupMetadata.KEY_WATCHLIST,
                        BackupMetadata.KEY_HISTORY,
                        BackupMetadata.KEY_CUSTOM_LISTS,
                        BackupMetadata.KEY_CUSTOM_LIST_ITEMS,
                        BackupMetadata.KEY_DIARY_ENTRIES,
                        BackupMetadata.KEY_SHOW_PROGRESS,
                        BackupMetadata.KEY_EPISODE_HISTORY,
                        BackupMetadata.KEY_CHALLENGES,
                        BackupMetadata.KEY_BLINDSPOTS
                    )
                    for (key in legacyKeys) {
                        val count = jsonObject.get("${key}Count")?.asInt
                            ?: jsonObject.getAsJsonArray(key)?.size()
                            ?: 0
                        if (count > 0) {
                            featureCounts[key] = count
                        }
                    }
                }

                val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                    fileName = BACKUP_FILE_NAME,
                    jsonPayload = json,
                    timestamp = timestamp,
                    deviceName = deviceName,
                    featureCounts = featureCounts
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
                googleAuthClient.ensureAuthenticatedSession()
                val effectiveUid = getEffectiveUserId()
                val doc = firestore.collection(colUserBackups).document(effectiveUid).get().await()
                if (doc.exists()) {
                    val featureCounts = mutableMapOf<String, Int>()
                    val legacyKeys = listOf(
                        BackupMetadata.KEY_FAVORITES,
                        BackupMetadata.KEY_WATCHLIST,
                        BackupMetadata.KEY_HISTORY,
                        BackupMetadata.KEY_CUSTOM_LISTS,
                        BackupMetadata.KEY_CUSTOM_LIST_ITEMS,
                        BackupMetadata.KEY_DIARY_ENTRIES,
                        BackupMetadata.KEY_SHOW_PROGRESS,
                        BackupMetadata.KEY_EPISODE_HISTORY,
                        BackupMetadata.KEY_CHALLENGES,
                        BackupMetadata.KEY_BLINDSPOTS
                    )
                    for (key in legacyKeys) {
                        doc.getLong("${key}Count")?.toInt()?.let { count ->
                            if (count > 0) {
                                featureCounts[key] = count
                            }
                        }
                    }
                    (doc.get("featureCounts") as? Map<*, *>)?.forEach { (k, v) ->
                        if (k is String && v is Number) {
                            featureCounts[k] = v.toInt()
                        }
                    }

                    val metadata = BackupMetadata(
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        formattedDate = doc.getString("formattedDate").orEmpty(),
                        sizeBytes = doc.getLong("sizeBytes") ?: 0L,
                        formattedSize = doc.getString("formattedSize").orEmpty(),
                        deviceName = doc.getString("deviceName").orEmpty(),
                        featureCounts = featureCounts
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
                            featureCounts = featureCounts
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
            val featuresPayload = JsonObject()
            val allFeatureCounts = mutableMapOf<String, Int>()

            for (contributor in contributors) {
                val data = contributor.exportData()
                if (data != null) {
                    featuresPayload.add(contributor.featureKey, data)
                }
                allFeatureCounts.putAll(contributor.getDetailedCounts())
            }

            _backupStatus.value = BackupStatus.InProgress(
                operation = BackupOperation.BACKUP,
                progressPercent = 50
            )

            val timestamp = System.currentTimeMillis()
            val snapshotObj = JsonObject()
            snapshotObj.addProperty("version", 3)
            snapshotObj.addProperty("timestamp", timestamp)
            snapshotObj.add("features", featuresPayload)

            val countsObj = JsonObject()
            allFeatureCounts.forEach { (k, v) ->
                countsObj.addProperty(k, v)
            }
            snapshotObj.add("featureCounts", countsObj)

            val jsonPayload = gson.toJson(snapshotObj)

            val fingerprintObj = snapshotObj.deepCopy()
            fingerprintObj.remove("timestamp")
            fingerprintObj.remove("deviceName")
            val payloadHash = computeSha256(gson.toJson(fingerprintObj))

            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = jsonPayload,
                timestamp = timestamp,
                featureCounts = allFeatureCounts
            )

            // SHA-256 Checksum Guard: Skip Firestore write if local payload hash is identical to last upload
            val lastUploadedHash = backupSettingsStorage.read(KEY_LAST_BACKUP_HASH, "")
            val isHashUnchanged = lastUploadedHash.isNotBlank() && lastUploadedHash == payloadHash

            if (!isHashUnchanged) {
                try {
                    googleAuthClient.ensureAuthenticatedSession()
                    val effectiveUid = getEffectiveUserId()
                    val firebaseUid = googleAuthClient.currentFirebaseAuthUid ?: effectiveUid
                    val base64GzipPayload = compressGzip(jsonPayload)
                    val backupDoc = mutableMapOf<String, Any>(
                        "uid" to effectiveUid,
                        "firebaseUid" to firebaseUid,
                        "version" to 3,
                        "timestamp" to timestamp,
                        "formattedDate" to metadata.formattedDate,
                        "deviceName" to metadata.deviceName,
                        "sizeBytes" to metadata.sizeBytes,
                        "formattedSize" to metadata.formattedSize,
                        "payloadHash" to payloadHash,
                        "isCompressed" to true,
                        "payloadGzip" to base64GzipPayload,
                        "featureCounts" to allFeatureCounts
                    )
                    allFeatureCounts.forEach { (k, v) ->
                        backupDoc["${k}Count"] = v
                    }

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
            try {
                googleAuthClient.ensureAuthenticatedSession()
                val effectiveUid = getEffectiveUserId()
                val doc = firestore.collection(colUserBackups).document(effectiveUid).get().await()
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

            val jsonObject = gson.fromJson(json, JsonObject::class.java)
            val featuresObj = jsonObject.getAsJsonObject("features")

            _backupStatus.value = BackupStatus.InProgress(
                operation = BackupOperation.RESTORE,
                progressPercent = 60
            )

            val allFeatureCounts = mutableMapOf<String, Int>()

            for (contributor in contributors) {
                val featurePayload = featuresObj?.get(contributor.featureKey)
                contributor.importData(featurePayload = featurePayload, fullSnapshot = jsonObject)
                allFeatureCounts.putAll(contributor.getDetailedCounts())
            }

            val timestamp = jsonObject.get("timestamp")?.asLong ?: System.currentTimeMillis()
            val deviceName = jsonObject.get("deviceName")?.asString.orEmpty()

            val (_, metadata) = googleDriveBackupClient.saveCompressedBackup(
                fileName = BACKUP_FILE_NAME,
                jsonPayload = json,
                timestamp = timestamp,
                deviceName = deviceName,
                featureCounts = allFeatureCounts
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
    }
}
