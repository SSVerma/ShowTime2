package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.DailyPollQuestion
import com.ssverma.shared.domain.model.community.DailyPollQuestionBank
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.repository.CommunityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val googleAuthClient: GoogleAuthClient,
    keyValueStorageClient: KeyValueStorageClient
) : CommunityRepository {

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "community_prefs")
    )

    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val installationIdKey = stringPreferencesKey("community_installation_id")
    private val keyPollCatalogJson = stringPreferencesKey("poll_catalog_json")
    private val keyPollCatalogVersion = intPreferencesKey("poll_catalog_version")
    private val keyPollCatalogLastFetched = longPreferencesKey("poll_catalog_last_fetched_epoch")
    private val keyPollEnabled = booleanPreferencesKey("poll_enabled")

    // In-memory cache for instant 0ms optimistic updates
    private val optimisticReactionsCache =
        ConcurrentHashMap<String, MutableStateFlow<MediaReactions>>()

    private val optimisticDailyPollCache =
        ConcurrentHashMap<String, MutableStateFlow<DailyPoll>>()

    init {
        // Trigger background catalog sync on startup
        repositoryScope.launch {
            syncCatalogIfStale()
        }
    }

    private suspend fun syncCatalogIfStale() {
        try {
            val lastFetched = storage.read(key = keyPollCatalogLastFetched, default = 0L)
            val now = System.currentTimeMillis()
            val isStale = (now - lastFetched) > TimeUnit.HOURS.toMillis(24)

            if (isStale) {
                val doc = firestore.collection("app_config")
                    .document("daily_polls_catalog")
                    .get()
                    .await()

                if (doc != null && doc.exists()) {
                    val remoteVersion = doc.getLong("catalogVersion")?.toInt() ?: 1
                    val isEnabled = doc.getBoolean("enabled") ?: true
                    val rawQuestions = doc.get("questions") as? List<*>

                    if (rawQuestions != null) {
                        val parsedQuestions = mutableListOf<DailyPollQuestion>()
                        for (item in rawQuestions) {
                            val map = item as? Map<*, *> ?: continue
                            val id = (map["id"] as? Number)?.toInt() ?: 0
                            val question = map["question"]?.toString().orEmpty()
                            val options =
                                (map["options"] as? List<*>)?.map { it.toString() } ?: emptyList()
                            val scheduledDate = map["scheduledDate"]?.toString()
                            if (question.isNotBlank() && options.isNotEmpty()) {
                                parsedQuestions.add(
                                    DailyPollQuestion(
                                        id = id,
                                        question = question,
                                        options = options,
                                        scheduledDate = scheduledDate
                                    )
                                )
                            }
                        }

                        if (parsedQuestions.isNotEmpty()) {
                            val jsonString = gson.toJson(parsedQuestions)
                            storage.write(key = keyPollCatalogJson, value = jsonString)
                            storage.write(key = keyPollCatalogVersion, value = remoteVersion)
                            storage.write(key = keyPollEnabled, value = isEnabled)
                            storage.write(key = keyPollCatalogLastFetched, value = now)
                        }
                    }
                } else {
                    // Seed questions from assets to Firestore if not yet populated on Firebase
                    val assetQuestions = loadQuestionsFromAsset()
                    if (assetQuestions.isNotEmpty()) {
                        val seedDoc = mapOf(
                            "catalogVersion" to 1,
                            "enabled" to true,
                            "updatedAt" to FieldValue.serverTimestamp(),
                            "questions" to assetQuestions.map { q ->
                                val map = mutableMapOf<String, Any>(
                                    "id" to q.id,
                                    "question" to q.question,
                                    "options" to q.options
                                )
                                q.scheduledDate?.let { map["scheduledDate"] = it }
                                map
                            }
                        )
                        firestore.collection("app_config")
                            .document("daily_polls_catalog")
                            .set(seedDoc)
                            .await()

                        storage.write(key = keyPollCatalogJson, value = gson.toJson(assetQuestions))
                        storage.write(key = keyPollCatalogVersion, value = 1)
                        storage.write(key = keyPollEnabled, value = true)
                        storage.write(key = keyPollCatalogLastFetched, value = now)
                    }
                }
            }
        } catch (_: Exception) {
            // Fail gracefully, use cached catalog
        }
    }

    private suspend fun getActiveQuestions(): List<DailyPollQuestion> {
        val cachedJson = storage.read(key = keyPollCatalogJson)
        if (!cachedJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<DailyPollQuestion>>() {}.type
                val parsed: List<DailyPollQuestion>? = gson.fromJson(cachedJson, type)
                if (!parsed.isNullOrEmpty()) {
                    return parsed
                }
            } catch (_: Exception) {
                // Ignore error, fallback to asset
            }
        }
        val assetQuestions = loadQuestionsFromAsset()
        if (assetQuestions.isNotEmpty()) {
            storage.write(key = keyPollCatalogJson, value = gson.toJson(assetQuestions))
            return assetQuestions
        }
        return emptyList()
    }

    private fun loadQuestionsFromAsset(): List<DailyPollQuestion> {
        return try {
            context.assets.open("daily_polls_catalog.json").use { inputStream ->
                val jsonString = inputStream.bufferedReader().readText()
                val jsonObject = gson.fromJson(jsonString, Map::class.java)
                val rawQuestions = jsonObject["questions"] as? List<*> ?: return emptyList()
                val parsed = mutableListOf<DailyPollQuestion>()
                for (item in rawQuestions) {
                    val map = item as? Map<*, *> ?: continue
                    val id = (map["id"] as? Number)?.toInt() ?: 0
                    val question = map["question"]?.toString().orEmpty()
                    val options = (map["options"] as? List<*>)?.map { it.toString() } ?: emptyList()
                    val scheduledDate = map["scheduledDate"]?.toString()
                    if (question.isNotBlank() && options.isNotEmpty()) {
                        parsed.add(
                            DailyPollQuestion(
                                id = id,
                                question = question,
                                options = options,
                                scheduledDate = scheduledDate
                            )
                        )
                    }
                }
                parsed
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun getEffectiveUserId(): String {
        val googleUser = googleAuthClient.currentUser.value
        if (googleUser != null && googleUser.email.isNotBlank()) {
            val sanitizedEmail = googleUser.email.replace(".", "_").replace("@", "_at_")
            return "user_$sanitizedEmail"
        }

        val cachedInstallationId = storage.read(key = installationIdKey)
        if (!cachedInstallationId.isNullOrBlank()) {
            return "anon_$cachedInstallationId"
        }

        val newId = UUID.randomUUID().toString().replace("-", "")
        storage.write(key = installationIdKey, value = newId)
        return "anon_$newId"
    }

    private fun getMediaDocKey(mediaType: MediaType, mediaId: Int): String {
        val typeStr = when (mediaType) {
            MediaType.Movie -> "movie"
            MediaType.Tv -> "tv"
            MediaType.Person -> "person"
            MediaType.Unknown -> "unknown"
        }
        return "${typeStr}_$mediaId"
    }

    private fun getUserDocKey(userId: String, mediaType: MediaType, mediaId: Int): String {
        val typeStr = when (mediaType) {
            MediaType.Movie -> "movie"
            MediaType.Tv -> "tv"
            MediaType.Person -> "person"
            MediaType.Unknown -> "unknown"
        }
        return "${userId}_${typeStr}_$mediaId"
    }

    override fun getMediaReactions(mediaType: MediaType, mediaId: Int): Flow<MediaReactions> {
        val mediaKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
        val optimisticFlow = optimisticReactionsCache.getOrPut(mediaKey) {
            MutableStateFlow(
                MediaReactions.empty(mediaType = mediaType, mediaId = mediaId)
            )
        }

        val aggregateFlow = callbackFlow {
            val docRef = firestore.collection("media_reactions").document(mediaKey)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val rawTagCounts = snapshot.get("tagCounts") as? Map<*, *>
                    val parsedTagCounts = mutableMapOf<MediaReactionTag, Int>()

                    rawTagCounts?.forEach { (k, v) ->
                        val tag = MediaReactionTag.fromTagKey(k.toString())
                        val count = (v as? Number)?.toInt() ?: 0
                        if (tag != null && count > 0) {
                            parsedTagCounts[tag] = count
                        }
                    }

                    val totalReactions = (snapshot.getLong("totalReactions"))?.toInt()
                        ?: parsedTagCounts.values.sum()

                    trySend(Pair(parsedTagCounts, totalReactions))
                } else {
                    trySend(Pair(emptyMap<MediaReactionTag, Int>(), 0))
                }
            }
            awaitClose { listener.remove() }
        }

        val userReactionFlow = callbackFlow {
            val userId = getEffectiveUserId()
            val userDocKey =
                getUserDocKey(userId = userId, mediaType = mediaType, mediaId = mediaId)
            val docRef = firestore.collection("user_media_reactions").document(userDocKey)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val rawSelectedTags = snapshot.get("selectedTags") as? List<*>
                    val selectedTags = rawSelectedTags?.mapNotNull {
                        MediaReactionTag.fromTagKey(it.toString())
                    }?.toSet() ?: emptySet()

                    trySend(selectedTags)
                } else {
                    trySend(emptySet())
                }
            }
            awaitClose { listener.remove() }
        }

        return combine(
            optimisticFlow,
            aggregateFlow,
            userReactionFlow
        ) { optimistic, (firestoreTagCounts, firestoreTotal), firestoreUserTags ->
            val mergedTagCounts = if (firestoreTagCounts.isNotEmpty()) {
                firestoreTagCounts
            } else {
                optimistic.tagCounts
            }

            val mergedTotal = if (firestoreTotal > 0) {
                firestoreTotal
            } else {
                optimistic.totalReactions
            }

            val mergedUserTags = if (firestoreUserTags.isNotEmpty()) {
                firestoreUserTags
            } else {
                optimistic.userSelectedTags
            }

            MediaReactions(
                mediaType = mediaType,
                mediaId = mediaId,
                tagCounts = mergedTagCounts,
                totalReactions = mergedTotal,
                userSelectedTags = mergedUserTags
            )
        }
    }

    override suspend fun toggleMediaReaction(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure> {
        return try {
            val mediaKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
            val userId = getEffectiveUserId()
            val userDocKey =
                getUserDocKey(userId = userId, mediaType = mediaType, mediaId = mediaId)

            val optimisticFlow = optimisticReactionsCache.getOrPut(mediaKey) {
                MutableStateFlow(MediaReactions.empty(mediaType = mediaType, mediaId = mediaId))
            }
            val current = optimisticFlow.value

            val isSelected = current.isTagSelected(tag = tag)
            val newSelectedTags = if (isSelected) {
                current.userSelectedTags - tag
            } else {
                current.userSelectedTags + tag
            }

            val currentCount = current.getCountForTag(tag = tag)
            val newCount = if (isSelected) {
                (currentCount - 1).coerceAtLeast(0)
            } else {
                currentCount + 1
            }

            val newTagCounts = current.tagCounts.toMutableMap().apply {
                if (newCount > 0) {
                    put(tag, newCount)
                } else {
                    remove(tag)
                }
            }

            val newTotal = if (isSelected) {
                (current.totalReactions - 1).coerceAtLeast(0)
            } else {
                current.totalReactions + 1
            }

            val optimisticUpdated = MediaReactions(
                mediaType = mediaType,
                mediaId = mediaId,
                tagCounts = newTagCounts,
                totalReactions = newTotal,
                userSelectedTags = newSelectedTags
            )

            // Emit instant 0ms local state update
            optimisticFlow.value = optimisticUpdated

            // Background Firestore Batch Update
            val batch = firestore.batch()
            val mediaDocRef = firestore.collection("media_reactions").document(mediaKey)
            val userDocRef = firestore.collection("user_media_reactions").document(userDocKey)

            val incrementVal = if (isSelected) -1L else 1L
            val tagFieldPath = "tagCounts.${tag.tagKey}"

            batch.set(
                mediaDocRef,
                mapOf(
                    tagFieldPath to FieldValue.increment(incrementVal),
                    "totalReactions" to FieldValue.increment(incrementVal),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            val userTagsUpdate = if (isSelected) {
                FieldValue.arrayRemove(tag.tagKey)
            } else {
                FieldValue.arrayUnion(tag.tagKey)
            }

            batch.set(
                userDocRef,
                mapOf(
                    "selectedTags" to userTagsUpdate,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            batch.commit().await()

            Result.Success(optimisticUpdated)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override fun getDailyPoll(date: LocalDate): Flow<DailyPoll> {
        val dateStr = date.toString()

        val optimisticFlow = optimisticDailyPollCache.getOrPut(dateStr) {
            MutableStateFlow(DailyPoll.empty(date))
        }

        val aggregateFlow = callbackFlow {
            val questions = getActiveQuestions()
            val resolvedQuestion = DailyPollQuestionBank.resolveQuestionForDate(
                date = date,
                questions = questions
            )
            val isEnabled = storage.read(key = keyPollEnabled, default = true)

            if (resolvedQuestion == null || !isEnabled) {
                trySend(Triple(null, emptyList<Int>(), 0))
                awaitClose { }
                return@callbackFlow
            }

            val docRef = firestore.collection("daily_polls").document(dateStr)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val rawVoteCounts = snapshot.get("voteCounts") as? Map<*, *>
                    val optionsCount = resolvedQuestion.options.size
                    val parsedVoteCounts = MutableList(optionsCount) { 0 }

                    rawVoteCounts?.forEach { (k, v) ->
                        val index = k.toString().toIntOrNull()
                        val count = (v as? Number)?.toInt() ?: 0
                        if (index != null && index in 0 until optionsCount) {
                            parsedVoteCounts[index] = count.coerceAtLeast(0)
                        }
                    }

                    // Also check for flat keys like "voteCounts.0"
                    for (i in 0 until optionsCount) {
                        val flatVal = snapshot.getLong("voteCounts.$i")?.toInt()
                        if (flatVal != null && parsedVoteCounts[i] == 0) {
                            parsedVoteCounts[i] = flatVal.coerceAtLeast(0)
                        }
                    }

                    val totalVotes = snapshot.getLong("totalVotes")?.toInt()
                        ?: parsedVoteCounts.sum()

                    trySend(Triple(resolvedQuestion, parsedVoteCounts.toList(), totalVotes))
                } else {
                    val initialCounts = List(resolvedQuestion.options.size) { 0 }
                    trySend(Triple(resolvedQuestion, initialCounts, 0))
                }
            }
            awaitClose { listener.remove() }
        }

        val userVoteFlow = callbackFlow {
            val userId = getEffectiveUserId()
            val userVoteDocKey = "${userId}_$dateStr"
            val docRef = firestore.collection("user_daily_poll_votes").document(userVoteDocKey)

            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val selectedIndex = snapshot.getLong("selectedOptionIndex")?.toInt()
                    trySend(selectedIndex)
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }

        return combine(
            optimisticFlow,
            aggregateFlow,
            userVoteFlow
        ) { optimistic, (resolvedQuestion, firestoreVoteCounts, firestoreTotal), firestoreUserSelection ->
            if (resolvedQuestion == null) {
                return@combine DailyPoll.empty(date)
            }

            val hasFirestoreCounts = firestoreVoteCounts.any { it > 0 }
            val mergedCounts =
                if (hasFirestoreCounts) firestoreVoteCounts else optimistic.voteCounts
            val mergedTotal = if (hasFirestoreCounts || firestoreTotal > 0) {
                maxOf(firestoreTotal, mergedCounts.sum())
            } else {
                optimistic.totalVotes
            }
            val mergedUserSelection = firestoreUserSelection ?: optimistic.selectedOptionIndex

            DailyPoll(
                dateString = dateStr,
                questionId = resolvedQuestion.id,
                question = resolvedQuestion.question,
                options = resolvedQuestion.options,
                voteCounts = if (mergedCounts.isEmpty()) List(resolvedQuestion.options.size) { 0 } else mergedCounts,
                totalVotes = mergedTotal,
                selectedOptionIndex = mergedUserSelection,
                isEnabled = optimistic.isEnabled
            )
        }
    }

    override suspend fun voteDailyPoll(
        date: LocalDate,
        optionIndex: Int
    ): Result<DailyPoll, Failure.CoreFailure> {
        return try {
            val dateStr = date.toString()
            val userId = getEffectiveUserId()
            val userVoteDocKey = "${userId}_$dateStr"

            val questions = getActiveQuestions()
            val resolvedQuestion = DailyPollQuestionBank.resolveQuestionForDate(
                date = date,
                questions = questions
            ) ?: return Result.Error(Failure.CoreFailure.UnexpectedFailure)

            val optimisticFlow = optimisticDailyPollCache.getOrPut(dateStr) {
                MutableStateFlow(
                    DailyPoll(
                        dateString = dateStr,
                        questionId = resolvedQuestion.id,
                        question = resolvedQuestion.question,
                        options = resolvedQuestion.options,
                        voteCounts = List(resolvedQuestion.options.size) { 0 },
                        totalVotes = 0,
                        selectedOptionIndex = null,
                        isEnabled = true
                    )
                )
            }

            val current = optimisticFlow.value
            if (current.selectedOptionIndex == optionIndex) {
                // Already voted for this option
                return Result.Success(current)
            }

            val oldIndex = current.selectedOptionIndex
            val newVoteCounts = current.voteCounts.toMutableList()

            // If switching vote or initial vote
            if (oldIndex != null && oldIndex in newVoteCounts.indices) {
                newVoteCounts[oldIndex] = (newVoteCounts[oldIndex] - 1).coerceAtLeast(0)
            }

            if (optionIndex in newVoteCounts.indices) {
                newVoteCounts[optionIndex] = newVoteCounts[optionIndex] + 1
            }

            val newTotalVotes = if (oldIndex == null) current.totalVotes + 1 else current.totalVotes

            val optimisticUpdated = DailyPoll(
                dateString = dateStr,
                questionId = resolvedQuestion.id,
                question = resolvedQuestion.question,
                options = resolvedQuestion.options,
                voteCounts = newVoteCounts.toList(),
                totalVotes = newTotalVotes,
                selectedOptionIndex = optionIndex,
                isEnabled = current.isEnabled
            )

            // 0ms Optimistic local update
            optimisticFlow.value = optimisticUpdated

            // Background Firestore Batch Write
            val batch = firestore.batch()
            val pollDocRef = firestore.collection("daily_polls").document(dateStr)
            val userDocRef = firestore.collection("user_daily_poll_votes").document(userVoteDocKey)

            val voteCountsMap = mutableMapOf<String, Any>(
                optionIndex.toString() to FieldValue.increment(1L)
            )
            if (oldIndex != null) {
                voteCountsMap[oldIndex.toString()] = FieldValue.increment(-1L)
            }

            batch.set(
                pollDocRef,
                mapOf(
                    "questionId" to resolvedQuestion.id,
                    "question" to resolvedQuestion.question,
                    "options" to resolvedQuestion.options,
                    "voteCounts" to voteCountsMap,
                    "totalVotes" to FieldValue.increment(if (oldIndex == null) 1L else 0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            batch.set(
                userDocRef,
                mapOf(
                    "selectedOptionIndex" to optionIndex,
                    "questionId" to resolvedQuestion.id,
                    "votedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            batch.commit().await()

            Result.Success(optimisticUpdated)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }
}

