package com.ssverma.shared.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import com.ssverma.shared.domain.repository.CinephileMilestoneRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinephileMilestoneRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    keyValueStorageClient: KeyValueStorageClient
) : CinephileMilestoneRepository {

    private val gson = Gson()

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "showtime_milestones_prefs")
    )

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix: String = if (isDebug) "dev_" else ""
    private val colCinephileMilestones: String get() = "${colPrefix}cinephile_milestones"

    private companion object {
        val KEY_MILESTONES_CATALOG_CACHE =
            stringPreferencesKey("cinephile_milestones_cached_json_v1")
        val KEY_MILESTONES_CATALOG_LAST_FETCHED =
            longPreferencesKey("cinephile_milestones_last_fetched_epoch")
        const val SEED_ASSET_FILE = "cinephile_milestones_catalog.json"
        const val DOC_ACTIVE_CATALOG = "active_catalog"
    }

    override val milestoneDefinitionsFlow: Flow<List<CinephileMilestoneDefinition>> =
        storage.observe(KEY_MILESTONES_CATALOG_CACHE)
            .map { cachedJson: String? ->
                if (!cachedJson.isNullOrBlank()) {
                    try {
                        val type = object : TypeToken<List<CinephileMilestoneDefinition>>() {}.type
                        gson.fromJson<List<CinephileMilestoneDefinition>>(cachedJson, type)
                            ?: emptyList()
                    } catch (_: Exception) {
                        loadMilestonesFromAsset()
                    }
                } else {
                    loadMilestonesFromAsset()
                }
            }
            .distinctUntilChanged()

    override suspend fun getMilestoneDefinitions(forceRefresh: Boolean): List<CinephileMilestoneDefinition> =
        withContext(Dispatchers.IO) {
            syncCatalogIfStale(forceRefresh = forceRefresh)
        }

    private suspend fun syncCatalogIfStale(forceRefresh: Boolean): List<CinephileMilestoneDefinition> {
        val now = System.currentTimeMillis()
        val lastFetched = storage.read(key = KEY_MILESTONES_CATALOG_LAST_FETCHED, default = 0L)
        val isStale = (now - lastFetched) > TimeUnit.HOURS.toMillis(24)

        if (!isStale && !forceRefresh) {
            val cached = getCachedMilestones()
            if (!cached.isNullOrEmpty()) {
                return cached
            }
        }

        try {
            val doc = firestore.collection(colCinephileMilestones)
                .document(DOC_ACTIVE_CATALOG)
                .get()
                .await()

            if (doc != null && doc.exists()) {
                val rawMilestones = doc.get("milestones")
                if (rawMilestones != null) {
                    val jsonString = gson.toJson(rawMilestones)
                    val type = object : TypeToken<List<CinephileMilestoneDefinition>>() {}.type
                    val parsed: List<CinephileMilestoneDefinition>? =
                        gson.fromJson(jsonString, type)
                    if (!parsed.isNullOrEmpty()) {
                        storage.write(
                            key = KEY_MILESTONES_CATALOG_CACHE,
                            value = gson.toJson(parsed)
                        )
                        storage.write(key = KEY_MILESTONES_CATALOG_LAST_FETCHED, value = now)
                        return parsed
                    }
                }
            } else if (isDebug) {
                // In Debug mode, auto-seed dev_cinephile_milestones if the document does not exist yet
                val seedList = loadMilestonesFromAsset()
                if (seedList.isNotEmpty()) {
                    try {
                        val seedDoc = mapOf(
                            "catalogVersion" to 1,
                            "enabled" to true,
                            "updatedAt" to FieldValue.serverTimestamp(),
                            "milestones" to seedList
                        )
                        firestore.collection(colCinephileMilestones)
                            .document(DOC_ACTIVE_CATALOG)
                            .set(seedDoc)
                            .await()
                    } catch (_: Exception) {
                        // Fail gracefully if dev environment lacks write permissions
                    }
                    storage.write(key = KEY_MILESTONES_CATALOG_CACHE, value = gson.toJson(seedList))
                    storage.write(key = KEY_MILESTONES_CATALOG_LAST_FETCHED, value = now)
                    return seedList
                }
            }
        } catch (_: Exception) {
            // Fail gracefully, use cached catalog or asset seed
        }

        return getCachedMilestones() ?: loadMilestonesFromAsset()
    }

    private suspend fun getCachedMilestones(): List<CinephileMilestoneDefinition>? {
        val cachedJson = storage.read(key = KEY_MILESTONES_CATALOG_CACHE)
        if (!cachedJson.isNullOrBlank()) {
            return try {
                val type = object : TypeToken<List<CinephileMilestoneDefinition>>() {}.type
                gson.fromJson<List<CinephileMilestoneDefinition>>(cachedJson, type)
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun loadMilestonesFromAsset(): List<CinephileMilestoneDefinition> {
        return try {
            context.assets.open(SEED_ASSET_FILE).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<CinephileMilestoneDefinition>>() {}.type
                    gson.fromJson(reader, type) ?: emptyList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
