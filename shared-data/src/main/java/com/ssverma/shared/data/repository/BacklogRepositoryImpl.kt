package com.ssverma.shared.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.data.local.adapter.MediaTypeJsonAdapter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.repository.BacklogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacklogRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    keyValueStorageClient: KeyValueStorageClient
) : BacklogRepository {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(MediaType::class.java, MediaTypeJsonAdapter())
        .create()

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "showtime_backlog_challenges_prefs")
    )

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix: String = if (isDebug) "dev_" else ""
    private val colCuratedChallenges: String get() = "${colPrefix}curated_challenges"

    private companion object {
        val KEY_ACTIVE_CHALLENGES = stringPreferencesKey("backlog_active_challenges_json")
        val KEY_BLINDSPOTS = stringPreferencesKey("backlog_blindspots_json")
        val KEY_CURATED_CHALLENGES_CACHE =
            stringPreferencesKey("backlog_curated_challenges_cached_json_v2")
        val KEY_CURATED_CHALLENGES_LAST_FETCHED =
            longPreferencesKey("backlog_curated_challenges_last_fetched_epoch")
        const val SEED_ASSET_FILE = "curated_challenges_seed.json"
        const val DOC_ACTIVE_CATALOG = "active_catalog"
    }

    override val curatedChallengesFlow: Flow<List<CinephileChallenge>> =
        storage.observe(KEY_CURATED_CHALLENGES_CACHE)
            .map { cachedJson ->
                if (!cachedJson.isNullOrBlank()) {
                    try {
                        val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                        gson.fromJson<List<CinephileChallenge>>(cachedJson, type) ?: emptyList()
                    } catch (_: Exception) {
                        loadCuratedChallengesFromAsset()
                    }
                } else {
                    loadCuratedChallengesFromAsset()
                }
            }
            .distinctUntilChanged()

    override val activeChallengesFlow: Flow<List<CinephileChallenge>> = storage.data.map { prefs ->
        val json = prefs[KEY_ACTIVE_CHALLENGES]
        if (json.isNullOrBlank()) {
            // Default: start with the top curated challenge joined
            val defaultList = loadCuratedChallengesFromAsset().take(1)
            defaultList
        } else {
            try {
                val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                gson.fromJson<List<CinephileChallenge>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override val blindspotsFlow: Flow<List<BlindspotPriorityItem>> = storage.data.map { prefs ->
        val json = prefs[KEY_BLINDSPOTS]
        if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<BlindspotPriorityItem>>() {}.type
                gson.fromJson<List<BlindspotPriorityItem>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getCuratedChallenges(forceRefresh: Boolean): List<CinephileChallenge> =
        withContext(Dispatchers.IO) {
            syncCatalogIfStale(forceRefresh = forceRefresh)
        }

    private suspend fun syncCatalogIfStale(forceRefresh: Boolean): List<CinephileChallenge> {
        val now = System.currentTimeMillis()
        val lastFetched = storage.read(key = KEY_CURATED_CHALLENGES_LAST_FETCHED, default = 0L)
        val isStale = (now - lastFetched) > TimeUnit.HOURS.toMillis(24)

        if (!isStale && !forceRefresh) {
            val cached = getCachedCuratedChallenges()
            if (!cached.isNullOrEmpty()) {
                return cached
            }
        }

        try {
            val doc = firestore.collection(colCuratedChallenges)
                .document(DOC_ACTIVE_CATALOG)
                .get()
                .await()

            if (doc != null && doc.exists()) {
                val rawChallenges = doc.get("challenges")
                if (rawChallenges != null) {
                    val jsonString = gson.toJson(rawChallenges)
                    val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                    val parsed: List<CinephileChallenge>? = gson.fromJson(jsonString, type)
                    if (!parsed.isNullOrEmpty()) {
                        storage.write(
                            key = KEY_CURATED_CHALLENGES_CACHE,
                            value = gson.toJson(parsed)
                        )
                        storage.write(key = KEY_CURATED_CHALLENGES_LAST_FETCHED, value = now)
                        return parsed
                    }
                }
            } else if (isDebug) {
                // In Debug mode, auto-seed dev_curated_challenges if the document does not exist yet
                val seedList = loadCuratedChallengesFromAsset()
                if (seedList.isNotEmpty()) {
                    try {
                        val seedDoc = mapOf(
                            "catalogVersion" to 1,
                            "enabled" to true,
                            "updatedAt" to FieldValue.serverTimestamp(),
                            "challenges" to seedList
                        )
                        firestore.collection(colCuratedChallenges)
                            .document(DOC_ACTIVE_CATALOG)
                            .set(seedDoc)
                            .await()
                    } catch (_: Exception) {
                        // Fail gracefully if dev environment lacks write permissions
                    }
                    storage.write(key = KEY_CURATED_CHALLENGES_CACHE, value = gson.toJson(seedList))
                    storage.write(key = KEY_CURATED_CHALLENGES_LAST_FETCHED, value = now)
                    return seedList
                }
            }
        } catch (_: Exception) {
            // Fail gracefully, use cached catalog or asset seed
        }

        val cached = getCachedCuratedChallenges()
        if (!cached.isNullOrEmpty()) {
            return cached
        }

        val assetList = loadCuratedChallengesFromAsset()
        if (assetList.isNotEmpty()) {
            storage.write(key = KEY_CURATED_CHALLENGES_CACHE, value = gson.toJson(assetList))
            return assetList
        }

        return emptyList()
    }

    private fun loadCuratedChallengesFromAsset(): List<CinephileChallenge> {
        return try {
            context.assets.open(SEED_ASSET_FILE).use { inputStream ->
                val jsonString = inputStream.bufferedReader().readText()
                val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                gson.fromJson<List<CinephileChallenge>>(jsonString, type) ?: emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getCachedCuratedChallenges(): List<CinephileChallenge>? {
        return try {
            val cachedJson = storage.read(key = KEY_CURATED_CHALLENGES_CACHE)
            if (!cachedJson.isNullOrBlank()) {
                val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                gson.fromJson<List<CinephileChallenge>>(cachedJson, type)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun joinChallenge(challenge: CinephileChallenge): Unit =
        withContext(Dispatchers.IO) {
            val current = activeChallengesFlow.first().toMutableList()
            current.removeAll { it.id == challenge.id }
            val joined = challenge.copy(joinedAt = System.currentTimeMillis())
            current.add(0, joined)
            storage.edit { prefs ->
                prefs[KEY_ACTIVE_CHALLENGES] = gson.toJson(current)
            }
        }

    override suspend fun leaveChallenge(challengeId: String): Unit = withContext(Dispatchers.IO) {
        val current = activeChallengesFlow.first().toMutableList()
        current.removeAll { it.id == challengeId }
        storage.edit { prefs ->
            prefs[KEY_ACTIVE_CHALLENGES] = gson.toJson(current)
        }
    }

    override suspend fun createCustomChallenge(
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem>
    ): CinephileChallenge = withContext(Dispatchers.IO) {
        val newChallenge = CinephileChallenge(
            id = "custom_${UUID.randomUUID()}",
            title = title,
            description = description,
            category = ChallengeCategory.PersonalGoal,
            mediaTypeFilter = mediaTypeFilter,
            targetCount = maxOf(targetCount, targetItems.size, 1),
            targetMediaItems = targetItems,
            isCustom = true,
            joinedAt = System.currentTimeMillis()
        )
        joinChallenge(newChallenge)
        newChallenge
    }

    override suspend fun deleteCustomChallenge(challengeId: String): Unit =
        withContext(Dispatchers.IO) {
            leaveChallenge(challengeId)
        }

    override suspend fun addBlindspot(item: BlindspotPriorityItem): Unit =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first().toMutableList()
            current.removeAll { it.mediaId == item.mediaId && it.mediaType == item.mediaType }
            current.add(0, item)
            storage.edit { prefs ->
                prefs[KEY_BLINDSPOTS] = gson.toJson(current)
            }
        }

    override suspend fun removeBlindspot(mediaId: Int, mediaType: MediaType): Unit =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first().toMutableList()
            current.removeAll { it.mediaId == mediaId && it.mediaType == mediaType }
            storage.edit { prefs ->
                prefs[KEY_BLINDSPOTS] = gson.toJson(current)
            }
        }

    override suspend fun isBlindspot(mediaId: Int, mediaType: MediaType): Boolean =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first()
            current.any { it.mediaId == mediaId && it.mediaType == mediaType }
        }
}
