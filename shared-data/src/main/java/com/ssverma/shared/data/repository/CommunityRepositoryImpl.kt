package com.ssverma.shared.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityCuratedListItem
import com.ssverma.shared.domain.model.community.CommunityListCategories
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.DailyPollQuestion
import com.ssverma.shared.domain.model.community.DailyPollQuestionBank
import com.ssverma.shared.domain.model.community.DeleteCommentParams
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.model.community.EditCommentParams
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.model.community.PostCommentParams
import com.ssverma.shared.domain.model.community.PublishCustomListParams
import com.ssverma.shared.domain.model.community.ReportCommentParams
import com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams
import com.ssverma.shared.domain.model.community.ToggleListUpvoteParams
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.domain.model.community.UnpublishCustomListParams
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
import kotlin.math.abs

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val googleAuthClient: GoogleAuthClient,
    private val appConfigProvider: AppConfigProvider,
    keyValueStorageClient: KeyValueStorageClient
) : CommunityRepository {

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "community_prefs")
    )

    private val gson = Gson()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix = if (isDebug) "dev_" else ""
    private val colDailyPollCatalog get() = "${colPrefix}daily_poll_catalog"
    private val colMediaReactions get() = "${colPrefix}media_reactions"
    private val colUserMediaReactions get() = "${colPrefix}user_media_reactions"
    private val colDailyPolls get() = "${colPrefix}daily_polls"
    private val colUserDailyPollVotes get() = "${colPrefix}user_daily_poll_votes"
    private val colMediaDiscussions get() = "${colPrefix}media_discussions"
    private val colCommunityCuratedLists get() = "${colPrefix}community_curated_lists"
    private val colUserListInteractions get() = "${colPrefix}user_list_interactions"

    private val installationIdKey = stringPreferencesKey("community_installation_id")
    private val keyPollCatalogJson = stringPreferencesKey("poll_catalog_json")
    private val keyPollCatalogVersion = intPreferencesKey("poll_catalog_version")
    private val keyPollCatalogLastFetched = longPreferencesKey("poll_catalog_last_fetched_epoch")
    private val keyPollEnabled = booleanPreferencesKey("poll_enabled")
    private val keyUpvotedLists = stringPreferencesKey("community_upvoted_list_ids")
    private val keyClonedLists = stringPreferencesKey("community_cloned_list_ids")

    private suspend fun getCachedUpvotedListIds(): MutableSet<String> {
        val json = storage.read(key = keyUpvotedLists) ?: return mutableSetOf()
        return try {
            gson.fromJson<Set<String>>(json, object : TypeToken<Set<String>>() {}.type)
                .toMutableSet()
        } catch (_: Exception) {
            mutableSetOf()
        }
    }

    private suspend fun getCachedClonedListIds(): MutableSet<String> {
        val json = storage.read(key = keyClonedLists) ?: return mutableSetOf()
        return try {
            gson.fromJson<Set<String>>(json, object : TypeToken<Set<String>>() {}.type)
                .toMutableSet()
        } catch (_: Exception) {
            mutableSetOf()
        }
    }

    // In-memory cache for instant 0ms optimistic updates
    private val optimisticReactionsCache =
        ConcurrentHashMap<String, MutableStateFlow<MediaReactions>>()

    private val optimisticDailyPollCache =
        ConcurrentHashMap<String, MutableStateFlow<DailyPoll>>()

    private val optimisticDiscussionsCache =
        ConcurrentHashMap<String, MutableStateFlow<List<Comment>>>()

    private data class CommentOverride(
        val upvotesCount: Int? = null,
        val isUpvotedByMe: Boolean? = null,
        val content: String? = null,
        val isSpoiler: Boolean? = null,
        val isEdited: Boolean? = null,
        val isDeleted: Boolean = false
    )

    private val optimisticCommentOverrides =
        ConcurrentHashMap<String, MutableStateFlow<Map<String, CommentOverride>>>()

    private data class ListOverride(
        val upvotesCount: Long? = null,
        val isUpvotedByMe: Boolean? = null,
        val clonesCount: Long? = null,
        val isClonedByMe: Boolean? = null,
        val isPublished: Boolean? = null
    )

    private val optimisticListOverrides =
        MutableStateFlow<Map<String, ListOverride>>(emptyMap())

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
                val doc = firestore.collection(colDailyPollCatalog)
                    .document("active_catalog")
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
                        firestore.collection(colDailyPollCatalog)
                            .document("active_catalog")
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

    private fun MediaType.toStorageKey(): String = when (this) {
        MediaType.Movie -> "movie"
        MediaType.Tv -> "tv"
        MediaType.Person -> "person"
        MediaType.Unknown -> "unknown"
    }

    override fun getMediaReactions(mediaType: MediaType, mediaId: Int): Flow<MediaReactions> {
        val mediaKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
        val optimisticFlow = optimisticReactionsCache.getOrPut(mediaKey) {
            MutableStateFlow(
                MediaReactions.empty(mediaType = mediaType, mediaId = mediaId)
            )
        }

        val aggregateFlow = callbackFlow {
            val docRef = firestore.collection(colMediaReactions).document(mediaKey)
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
            val docRef = firestore.collection(colUserMediaReactions).document(userDocKey)

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

        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_COMMUNITY_REACTIONS_ENABLED, true)

        return combine(
            optimisticFlow,
            aggregateFlow,
            userReactionFlow,
            remoteEnabledFlow
        ) { optimistic, (firestoreTagCounts, firestoreTotal), firestoreUserTags, isRemotelyEnabled ->
            if (!isRemotelyEnabled) {
                return@combine MediaReactions.empty(mediaType = mediaType, mediaId = mediaId)
                    .copy(isEnabled = false)
            }

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
                userSelectedTags = mergedUserTags,
                isEnabled = isRemotelyEnabled
            )
        }
    }

    override suspend fun toggleMediaReaction(
        mediaType: MediaType,
        mediaId: Int,
        tag: MediaReactionTag
    ): Result<MediaReactions, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_REACTIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
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
            val mediaDocRef = firestore.collection(colMediaReactions).document(mediaKey)
            val userDocRef = firestore.collection(colUserMediaReactions).document(userDocKey)

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

            val docRef = firestore.collection(colDailyPolls).document(dateStr)
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
            val docRef = firestore.collection(colUserDailyPollVotes).document(userVoteDocKey)

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

        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_DAILY_POLLS_ENABLED, true)

        return combine(
            optimisticFlow,
            aggregateFlow,
            userVoteFlow,
            remoteEnabledFlow
        ) { optimistic, (resolvedQuestion, firestoreVoteCounts, firestoreTotal), firestoreUserSelection, isRemotelyEnabled ->
            if (resolvedQuestion == null || !isRemotelyEnabled) {
                return@combine DailyPoll.empty(date).copy(isEnabled = false)
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
                isEnabled = optimistic.isEnabled && isRemotelyEnabled
            )
        }
    }

    override suspend fun voteDailyPoll(
        date: LocalDate,
        optionIndex: Int
    ): Result<DailyPoll, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_DAILY_POLLS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
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
            if (current.selectedOptionIndex != null) {
                // Already voted today; lock vote to prevent extra writes
                return Result.Success(current)
            }

            val newVoteCounts = current.voteCounts.toMutableList()

            // If switching vote or initial vote
            if (optionIndex in newVoteCounts.indices) {
                newVoteCounts[optionIndex] = newVoteCounts[optionIndex] + 1
            }

            val newTotalVotes = current.totalVotes + 1

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
            val pollDocRef = firestore.collection(colDailyPolls).document(dateStr)
            val userDocRef = firestore.collection(colUserDailyPollVotes).document(userVoteDocKey)

            val voteCountsMap = mutableMapOf<String, Any>(
                optionIndex.toString() to FieldValue.increment(1L)
            )

            batch.set(
                pollDocRef,
                mapOf(
                    "questionId" to resolvedQuestion.id,
                    "question" to resolvedQuestion.question,
                    "options" to resolvedQuestion.options,
                    "voteCounts" to voteCountsMap,
                    "totalVotes" to FieldValue.increment(1L),
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

    override fun getDiscussions(
        target: DiscussionTarget
    ): Flow<List<Comment>> {
        val pathKey = getDiscussionPathKey(target)
        val optimisticFlow = optimisticDiscussionsCache.getOrPut(pathKey) {
            MutableStateFlow(emptyList())
        }

        val firestoreFlow = callbackFlow {
            val userId = getEffectiveUserId()
            val commentsCollection = firestore
                .collection(colMediaDiscussions)
                .document(pathKey)
                .collection("comments")
                .orderBy(
                    "createdAtEpochMs",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .limit(50)

            val listener = commentsCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val authorId = doc.getString("authorId") ?: return@mapNotNull null
                        val authorName = doc.getString("authorName") ?: "Cinephile"
                        val authorAvatarUrl = doc.getString("authorAvatarUrl")
                        val content = doc.getString("content") ?: return@mapNotNull null
                        val isSpoiler = doc.getBoolean("isSpoiler") ?: false
                        val upvotesCount = doc.getLong("upvotesCount")?.toInt() ?: 0
                        val upvoterIds = doc.get("upvoterIds") as? List<*> ?: emptyList<Any>()
                        val isUpvotedByMe = upvoterIds.any { it.toString() == userId }
                        val isOwner = authorId == userId
                        val isEdited = doc.getBoolean("isEdited") ?: false
                        val parentId = doc.getString("parentId")
                        val replyToAuthorName = doc.getString("replyToAuthorName")
                        val repliesCount = doc.getLong("repliesCount")?.toInt() ?: 0
                        val reportCount = doc.getLong("reportCount")?.toInt() ?: 0
                        val createdAtEpochMs = doc.getLong("createdAtEpochMs")
                            ?: doc.getTimestamp("createdAt")?.toDate()?.time
                            ?: System.currentTimeMillis()

                        // Filter out reported content if report count >= 5
                        if (reportCount < 5) {
                            Comment(
                                id = id,
                                authorId = authorId,
                                authorName = authorName,
                                authorAvatarUrl = authorAvatarUrl,
                                content = content,
                                isSpoiler = isSpoiler,
                                upvotesCount = upvotesCount,
                                isUpvotedByMe = isUpvotedByMe,
                                isOwner = isOwner,
                                isEdited = isEdited,
                                parentId = parentId,
                                replyToAuthorName = replyToAuthorName,
                                repliesCount = repliesCount,
                                reportCount = reportCount,
                                createdAtEpochMs = createdAtEpochMs
                            )
                        } else null
                    }
                    trySend(comments)
                }
            }
            awaitClose { listener.remove() }
        }

        val overridesFlow = optimisticCommentOverrides.getOrPut(pathKey) {
            MutableStateFlow(emptyMap())
        }

        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)

        return combine(
            optimisticFlow,
            firestoreFlow,
            overridesFlow,
            remoteEnabledFlow
        ) { optimistic, firestoreList, overrides, isRemotelyEnabled ->
            if (!isRemotelyEnabled) {
                return@combine emptyList<Comment>()
            }

            val firestoreIds = firestoreList.map { it.id }.toSet()
            val pendingLocal = optimistic.filter { it.id !in firestoreIds }
            val combined = pendingLocal + firestoreList

            val allFlat = combined.mapNotNull { comment ->
                val override = overrides[comment.id]
                if (override?.isDeleted == true) {
                    null
                } else if (override != null) {
                    comment.copy(
                        upvotesCount = override.upvotesCount ?: comment.upvotesCount,
                        isUpvotedByMe = override.isUpvotedByMe ?: comment.isUpvotedByMe,
                        content = override.content ?: comment.content,
                        isSpoiler = override.isSpoiler ?: comment.isSpoiler,
                        isEdited = override.isEdited ?: comment.isEdited
                    )
                } else {
                    comment
                }
            }

            val allIds = allFlat.map { it.id }.toSet()
            // Direct roots have parentId == null. If parentId points to an ID not in allFlat, treat as root so it's not lost.
            val roots = allFlat.filter { it.parentId == null || it.parentId !in allIds }
            val childrenByParent = allFlat.filter { it.parentId != null && it.parentId in allIds }
                .groupBy { it.parentId!! }

            fun getAllDescendants(
                parentId: String,
                visited: Set<String> = emptySet()
            ): List<Comment> {
                if (parentId in visited) return emptyList()
                val direct = childrenByParent[parentId].orEmpty()
                return direct + direct.flatMap { getAllDescendants(it.id, visited + parentId) }
            }

            roots.map { root ->
                val threadReplies =
                    getAllDescendants(root.id).distinctBy { it.id }.sortedBy { it.createdAtEpochMs }
                root.copy(
                    replies = threadReplies,
                    repliesCount = if (threadReplies.isNotEmpty()) threadReplies.size else root.repliesCount
                )
            }
        }
    }

    override suspend fun postComment(
        params: PostCommentParams
    ): Result<Comment, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val pathKey = getDiscussionPathKey(params.target)
            val userId = getEffectiveUserId()
            val googleUser = googleAuthClient.currentUser.value
            val authorName = googleUser?.displayName?.takeIf { it.isNotBlank() }
                ?: googleUser?.email?.substringBefore("@")
                ?: "Cinephile #${abs(userId.hashCode() % 900) + 100}"
            val authorAvatarUrl = googleUser?.photoUrl

            val commentId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val newComment = Comment(
                id = commentId,
                authorId = userId,
                authorName = authorName,
                authorAvatarUrl = authorAvatarUrl,
                content = params.content.trim(),
                isSpoiler = params.isSpoiler,
                upvotesCount = 0,
                isUpvotedByMe = false,
                isOwner = true,
                isEdited = false,
                parentId = params.parentId,
                replyToAuthorName = params.replyToAuthorName,
                repliesCount = 0,
                reportCount = 0,
                createdAtEpochMs = now
            )

            // 0ms Optimistic local update
            val optimisticFlow = optimisticDiscussionsCache.getOrPut(pathKey) {
                MutableStateFlow(emptyList())
            }
            optimisticFlow.value = listOf(newComment) + optimisticFlow.value

            // Save to Firestore
            val commentDoc = mutableMapOf(
                "authorId" to userId,
                "authorName" to authorName,
                "authorAvatarUrl" to authorAvatarUrl,
                "content" to params.content.trim(),
                "isSpoiler" to params.isSpoiler,
                "upvotesCount" to 0L,
                "upvoterIds" to emptyList<String>(),
                "parentId" to params.parentId,
                "replyToAuthorName" to params.replyToAuthorName,
                "repliesCount" to 0L,
                "reportCount" to 0L,
                "isEdited" to false,
                "createdAtEpochMs" to now,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val mediaDoc = firestore.collection(colMediaDiscussions).document(pathKey)
            val mediaMetadata = mutableMapOf<String, Any>(
                "mediaId" to params.target.mediaId,
                "mediaType" to if (params.target.mediaType == MediaType.Tv) "tv" else "movie",
                "discussionCount" to FieldValue.increment(1L),
                "latestCommentSnippet" to params.content.trim().take(100),
                "lastCommentedAt" to FieldValue.serverTimestamp()
            )
            params.mediaTitle?.let { mediaMetadata["title"] = it }
            params.posterImageUrl?.let { mediaMetadata["posterImageUrl"] = it }
            params.backdropImageUrl?.let { mediaMetadata["backdropImageUrl"] = it }
            params.target.seasonNumber?.let { mediaMetadata["seasonNumber"] = it }
            params.target.episodeNumber?.let { mediaMetadata["episodeNumber"] = it }

            val batch = firestore.batch()
            batch.set(mediaDoc, mediaMetadata, SetOptions.merge())
            val parentId = params.parentId
            if (parentId != null) {
                val parentDoc = mediaDoc.collection("comments").document(parentId)
                batch.set(
                    parentDoc,
                    mapOf("repliesCount" to FieldValue.increment(1L)),
                    SetOptions.merge()
                )
            }
            batch.set(mediaDoc.collection("comments").document(commentId), commentDoc)
            batch.commit().await()

            Result.Success(newComment)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun editComment(
        params: EditCommentParams
    ): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val pathKey = getDiscussionPathKey(params.target)
            val userId = getEffectiveUserId()

            // 0ms Optimistic local update
            val overridesFlow = optimisticCommentOverrides.getOrPut(pathKey) {
                MutableStateFlow(emptyMap())
            }
            overridesFlow.value += (params.commentId to CommentOverride(
                content = params.newContent.trim(),
                isSpoiler = params.isSpoiler,
                isEdited = true
            ))

            val commentDocRef = firestore
                .collection(colMediaDiscussions)
                .document(pathKey)
                .collection("comments")
                .document(params.commentId)

            val doc = commentDocRef.get().await()
            if (doc.getString("authorId") == userId) {
                commentDocRef.set(
                    mapOf(
                        "content" to params.newContent.trim(),
                        "isSpoiler" to params.isSpoiler,
                        "isEdited" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun reportComment(
        params: ReportCommentParams
    ): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val pathKey = getDiscussionPathKey(params.target)
            val userId = getEffectiveUserId()

            val commentDocRef = firestore
                .collection(colMediaDiscussions)
                .document(pathKey)
                .collection("comments")
                .document(params.commentId)

            val batch = firestore.batch()
            batch.update(commentDocRef, "reportCount", FieldValue.increment(1L))
            val reportDoc =
                commentDocRef.collection("reports").document(UUID.randomUUID().toString())
            batch.set(
                reportDoc,
                mapOf(
                    "reporterId" to userId,
                    "reason" to params.reason,
                    "reportedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun toggleCommentUpvote(
        params: ToggleCommentUpvoteParams
    ): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val pathKey = getDiscussionPathKey(params.target)
            val userId = getEffectiveUserId()

            val commentDocRef = firestore
                .collection(colMediaDiscussions)
                .document(pathKey)
                .collection("comments")
                .document(params.commentId)

            val overridesFlow = optimisticCommentOverrides.getOrPut(pathKey) {
                MutableStateFlow(emptyMap())
            }
            val currentOverrides = overridesFlow.value
            val existingOverride = currentOverrides[params.commentId]

            val docSnapshot = try {
                commentDocRef.get().await()
            } catch (_: Exception) {
                null
            }
            val upvoterIds =
                (docSnapshot?.get("upvoterIds") as? List<*>)?.map { it.toString() } ?: emptyList()
            val isAlreadyUpvotedInDb = upvoterIds.contains(userId)
            val currentCountInDb = docSnapshot?.getLong("upvotesCount")?.toInt() ?: 0

            val currentIsUpvoted = existingOverride?.isUpvotedByMe ?: isAlreadyUpvotedInDb
            val currentCount = existingOverride?.upvotesCount ?: currentCountInDb

            val newIsUpvoted = !currentIsUpvoted
            val newCount =
                if (newIsUpvoted) currentCount + 1 else (currentCount - 1).coerceAtLeast(0)

            // 0ms Optimistic local update
            overridesFlow.value = currentOverrides + (params.commentId to CommentOverride(
                upvotesCount = newCount,
                isUpvotedByMe = newIsUpvoted
            ))

            // Resilient Firestore update
            if (newIsUpvoted) {
                commentDocRef.set(
                    mapOf(
                        "upvotesCount" to FieldValue.increment(1L),
                        "upvoterIds" to FieldValue.arrayUnion(userId)
                    ),
                    SetOptions.merge()
                ).await()
            } else {
                commentDocRef.set(
                    mapOf(
                        "upvotesCount" to FieldValue.increment(-1L),
                        "upvoterIds" to FieldValue.arrayRemove(userId)
                    ),
                    SetOptions.merge()
                ).await()
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun deleteComment(
        params: DeleteCommentParams
    ): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val pathKey = getDiscussionPathKey(params.target)
            val userId = getEffectiveUserId()

            // 0ms Optimistic local update
            val overridesFlow = optimisticCommentOverrides.getOrPut(pathKey) {
                MutableStateFlow(emptyMap())
            }
            overridesFlow.value += (params.commentId to CommentOverride(isDeleted = true))

            val commentDocRef = firestore
                .collection(colMediaDiscussions)
                .document(pathKey)
                .collection("comments")
                .document(params.commentId)

            val doc = commentDocRef.get().await()
            if (doc.getString("authorId") == userId) {
                val mediaDoc = firestore.collection(colMediaDiscussions).document(pathKey)
                val batch = firestore.batch()
                batch.delete(commentDocRef)
                batch.set(
                    mediaDoc,
                    mapOf("discussionCount" to FieldValue.increment(-1L)),
                    SetOptions.merge()
                )
                batch.commit().await()
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override fun getTrendingDiscussions(): Flow<List<TrendingDiscussion>> {
        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED, true)

        return callbackFlow {
            val listener = firestore
                .collection(colMediaDiscussions)
                .whereGreaterThan("discussionCount", 0)
                .orderBy(
                    "discussionCount",
                    com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    val trendingList = snapshot?.documents.orEmpty().mapNotNull { doc ->
                        val mediaId = doc.getLong("mediaId")?.toInt() ?: return@mapNotNull null
                        val mediaTypeStr = doc.getString("mediaType") ?: "movie"
                        val mediaType = if (mediaTypeStr == "tv") MediaType.Tv else MediaType.Movie
                        val title = doc.getString("title") ?: "Discussion"
                        val backdropImageUrl = doc.getString("backdropImageUrl")
                        val posterImageUrl = doc.getString("posterImageUrl")
                        val discussionCount = doc.getLong("discussionCount")?.toInt() ?: 0
                        val latestSnippet = doc.getString("latestCommentSnippet") ?: ""
                        val seasonNumber = doc.getLong("seasonNumber")?.toInt()
                        val episodeNumber = doc.getLong("episodeNumber")?.toInt()

                        if (discussionCount > 0) {
                            TrendingDiscussion(
                                mediaId = mediaId,
                                mediaType = mediaType,
                                title = title,
                                backdropImageUrl = backdropImageUrl,
                                posterImageUrl = posterImageUrl,
                                discussionCount = discussionCount,
                                latestCommentSnippet = latestSnippet,
                                seasonNumber = seasonNumber,
                                episodeNumber = episodeNumber
                            )
                        } else null
                    }
                    trySend(trendingList)
                }
            awaitClose { listener.remove() }
        }.combine(remoteEnabledFlow) { trendingList, isRemotelyEnabled ->
            if (!isRemotelyEnabled) emptyList() else trendingList
        }
    }

    override fun getCommunityCuratedLists(category: String?): Flow<List<CommunityCuratedList>> {
        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)

        return callbackFlow {
            val currentUserId = getEffectiveUserId()
            val upvotedSet = getCachedUpvotedListIds()
            val clonedSet = getCachedClonedListIds()
            val collectionRef = firestore.collection(colCommunityCuratedLists)
                .whereEqualTo("isPublished", true)
                .limit(50)

            val listener = collectionRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val listId = doc.getString("listId") ?: doc.id
                    doc.toCommunityCuratedList(
                        currentUserId = currentUserId,
                        isUpvoted = upvotedSet.contains(listId),
                        isCloned = clonedSet.contains(listId)
                    )
                }

                trySend(list)
            }

            awaitClose { listener.remove() }
        }.combine(optimisticListOverrides) { rawList, overrides ->
            // Sort raw snapshot lists first so optimistic like/clone interactions don't jump card positions
            val sortedRaw = rawList.sortedWith(
                compareByDescending<CommunityCuratedList> { it.upvotesCount * 2 + it.clonesCount }
                    .thenByDescending { it.createdAtEpochMs }
            )

            sortedRaw.mapNotNull { list ->
                val override = overrides[list.listId]
                if (override?.isPublished == false) {
                    null
                } else {
                    list.copy(
                        upvotesCount = override?.upvotesCount ?: list.upvotesCount,
                        isUpvotedByMe = override?.isUpvotedByMe ?: list.isUpvotedByMe,
                        clonesCount = override?.clonesCount ?: list.clonesCount,
                        isClonedByMe = override?.isClonedByMe ?: list.isClonedByMe
                    )
                }
            }.filter { list ->
                if (category.isNullOrBlank() || category == CommunityListCategories.ALL) {
                    true
                } else {
                    list.categoryTag.equals(category, ignoreCase = true)
                }
            }
        }.combine(remoteEnabledFlow) { list, isRemotelyEnabled ->
            if (!isRemotelyEnabled) emptyList() else list
        }
    }

    override fun getCommunityListDetails(listId: String): Flow<CommunityCuratedList?> {
        val remoteEnabledFlow =
            appConfigProvider.observeBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)

        return callbackFlow {
            val currentUserId = getEffectiveUserId()
            val upvotedSet = getCachedUpvotedListIds()
            val clonedSet = getCachedClonedListIds()
            val docRef = firestore.collection(colCommunityCuratedLists).document(listId)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(
                    snapshot.toCommunityCuratedList(
                        currentUserId = currentUserId,
                        isUpvoted = upvotedSet.contains(listId),
                        isCloned = clonedSet.contains(listId)
                    )
                )
            }
            awaitClose { listener.remove() }
        }.combine(optimisticListOverrides) { rawList, overrides ->
            if (rawList == null) null
            else {
                val override = overrides[rawList.listId]
                if (override?.isPublished == false) null
                else {
                    rawList.copy(
                        upvotesCount = override?.upvotesCount ?: rawList.upvotesCount,
                        isUpvotedByMe = override?.isUpvotedByMe ?: rawList.isUpvotedByMe,
                        clonesCount = override?.clonesCount ?: rawList.clonesCount,
                        isClonedByMe = override?.isClonedByMe ?: rawList.isClonedByMe
                    )
                }
            }
        }.combine(remoteEnabledFlow) { details, isRemotelyEnabled ->
            if (!isRemotelyEnabled) null else details
        }
    }

    override suspend fun publishCustomList(params: PublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val currentUserId = getEffectiveUserId()
            val user = googleAuthClient.currentUser.value
            val authorName =
                user?.displayName ?: "Cinephile #${abs(currentUserId.hashCode() % 1000)}"
            val authorAvatarUrl = user?.photoUrl

            val local = params.localList
            val itemsData = local.items.mapIndexed { index, item ->
                mapOf(
                    "mediaId" to item.mediaId,
                    "mediaType" to item.mediaType.toStorageKey(),
                    "title" to item.title,
                    "posterImageUrl" to item.posterImageUrl,
                    "backdropImageUrl" to item.backdropImageUrl,
                    "voteAvg" to item.voteAvg.toDouble(),
                    "rankOrder" to index
                )
            }

            val docData = hashMapOf(
                "listId" to local.listId,
                "title" to local.title,
                "description" to (local.description ?: ""),
                "authorId" to currentUserId,
                "authorName" to authorName,
                "authorAvatarUrl" to (authorAvatarUrl ?: ""),
                "categoryTag" to params.categoryTag,
                "itemCount" to local.items.size.toLong(),
                "items" to itemsData,
                "previewPosters" to local.previewPosters,
                "upvotesCount" to 0L,
                "clonesCount" to 0L,
                "isPublished" to true,
                "createdAt" to local.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            // Update optimistic overrides immediately
            val currentOverrides = optimisticListOverrides.value.toMutableMap()
            currentOverrides[local.listId] = ListOverride(isPublished = true)
            optimisticListOverrides.value = currentOverrides

            firestore.collection(colCommunityCuratedLists)
                .document(local.listId)
                .set(docData, SetOptions.merge())
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun unpublishCustomList(params: UnpublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            // Optimistic 0ms update
            val currentOverrides = optimisticListOverrides.value.toMutableMap()
            currentOverrides[params.listId] = ListOverride(isPublished = false)
            optimisticListOverrides.value = currentOverrides

            firestore.collection(colCommunityCuratedLists)
                .document(params.listId)
                .update("isPublished", false)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun toggleCommunityListUpvote(params: ToggleListUpvoteParams): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val currentUserId = getEffectiveUserId()
            val upvotedSet = getCachedUpvotedListIds()
            val isAlreadyUpvoted = optimisticListOverrides.value[params.listId]?.isUpvotedByMe
                ?: upvotedSet.contains(params.listId)
            val newUpvoted = !isAlreadyUpvoted

            if (newUpvoted) {
                upvotedSet.add(params.listId)
            } else {
                upvotedSet.remove(params.listId)
            }
            storage.write(key = keyUpvotedLists, value = gson.toJson(upvotedSet))

            // 0ms Optimistic UI update
            val currentOverrides = optimisticListOverrides.value.toMutableMap()
            val existingOverride = currentOverrides[params.listId]
            val currentUpvotes = existingOverride?.upvotesCount
            val delta = if (newUpvoted) 1L else -1L
            val nextUpvotes = currentUpvotes?.let { (it + delta).coerceAtLeast(0L) }
            currentOverrides[params.listId] = (existingOverride ?: ListOverride()).copy(
                isUpvotedByMe = newUpvoted,
                upvotesCount = nextUpvotes
            )
            optimisticListOverrides.value = currentOverrides

            repositoryScope.launch {
                try {
                    val listRef =
                        firestore.collection(colCommunityCuratedLists).document(params.listId)
                    val userInteractionRef = firestore.collection(colUserListInteractions)
                        .document("${currentUserId}_${params.listId}")
                    val batch = firestore.batch()
                    batch.set(
                        userInteractionRef,
                        mapOf(
                            "userId" to currentUserId,
                            "listId" to params.listId,
                            "isUpvoted" to newUpvoted,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )

                    batch.update(
                        listRef,
                        "upvotesCount",
                        FieldValue.increment(delta)
                    )

                    batch.commit().await()
                } catch (_: Exception) {
                    // Non-blocking firestore sync
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    override suspend fun recordListClone(listId: String): Result<Unit, Failure.CoreFailure> {
        if (!appConfigProvider.getBoolean(REMOTE_KEY_COMMUNITY_LISTS_ENABLED, true)) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        return try {
            val currentUserId = getEffectiveUserId()
            val clonedSet = getCachedClonedListIds()
            clonedSet.add(listId)
            storage.write(key = keyClonedLists, value = gson.toJson(clonedSet))

            // 0ms Optimistic UI update
            val currentOverrides = optimisticListOverrides.value.toMutableMap()
            val existingOverride = currentOverrides[listId]
            val currentClones = existingOverride?.clonesCount
            val nextClones = currentClones?.let { it + 1L }
            currentOverrides[listId] = (existingOverride ?: ListOverride()).copy(
                isClonedByMe = true,
                clonesCount = nextClones
            )
            optimisticListOverrides.value = currentOverrides

            repositoryScope.launch {
                try {
                    val batch = firestore.batch()
                    val userInteractionRef = firestore.collection(colUserListInteractions)
                        .document("${currentUserId}_${listId}")
                    batch.set(
                        userInteractionRef,
                        mapOf(
                            "userId" to currentUserId,
                            "listId" to listId,
                            "isCloned" to true,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    batch.update(
                        firestore.collection(colCommunityCuratedLists).document(listId),
                        "clonesCount",
                        FieldValue.increment(1L)
                    )
                    batch.commit().await()
                } catch (_: Exception) {
                    // Non-blocking firestore sync
                }
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    private fun DocumentSnapshot.toCommunityCuratedList(
        currentUserId: String,
        isUpvoted: Boolean = false,
        isCloned: Boolean = false
    ): CommunityCuratedList? {
        val listId = getString("listId") ?: id
        val title = getString("title") ?: return null
        val description = getString("description")
        val authorId = getString("authorId") ?: "unknown"
        val authorName = getString("authorName") ?: "Cinephile"
        val authorAvatarUrl = getString("authorAvatarUrl")
        val categoryTag = getString("categoryTag") ?: "Cinephile Favorites"
        val itemCount = getLong("itemCount")?.toInt() ?: 0
        val upvotesCount = getLong("upvotesCount") ?: 0L
        val clonesCount = getLong("clonesCount") ?: 0L
        val createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        val updatedAt = getLong("updatedAt") ?: System.currentTimeMillis()

        @Suppress("UNCHECKED_CAST")
        val rawItems = get("items") as? List<Map<String, Any>> ?: emptyList()
        val items = rawItems.mapNotNull { itemMap ->
            val mediaId = (itemMap["mediaId"] as? Number)?.toInt() ?: return@mapNotNull null
            val mediaTypeStr = itemMap["mediaType"] as? String ?: "movie"
            val mediaType = if (mediaTypeStr == "tv") MediaType.Tv else MediaType.Movie
            val itemTitle = itemMap["title"] as? String ?: ""
            val posterImageUrl = itemMap["posterImageUrl"] as? String ?: ""
            val backdropImageUrl = itemMap["backdropImageUrl"] as? String ?: ""
            val voteAvg = (itemMap["voteAvg"] as? Number)?.toFloat() ?: 0f
            val rankOrder = (itemMap["rankOrder"] as? Number)?.toInt() ?: 0

            CommunityCuratedListItem(
                mediaId = mediaId,
                mediaType = mediaType,
                title = itemTitle,
                posterImageUrl = posterImageUrl,
                backdropImageUrl = backdropImageUrl,
                voteAvg = voteAvg,
                rankOrder = rankOrder
            )
        }

        @Suppress("UNCHECKED_CAST")
        val previewPosters = get("previewPosters") as? List<String>
            ?: items.map { it.posterImageUrl }.filter { it.isNotBlank() }.take(4)

        return CommunityCuratedList(
            listId = listId,
            title = title,
            description = description,
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            categoryTag = categoryTag,
            itemCount = itemCount.coerceAtLeast(items.size),
            items = items,
            previewPosters = previewPosters,
            upvotesCount = upvotesCount,
            clonesCount = clonesCount,
            isMine = (authorId == currentUserId),
            isUpvotedByMe = isUpvoted,
            isClonedByMe = isCloned,
            createdAtEpochMs = createdAt,
            updatedAtEpochMs = updatedAt
        )
    }

    private fun getDiscussionPathKey(
        target: DiscussionTarget
    ): String {
        return if (target.seasonNumber != null && target.episodeNumber != null) {
            "tv_${target.mediaId}_s${target.seasonNumber}_e${target.episodeNumber}"
        } else {
            val typeStr = when (target.mediaType) {
                MediaType.Movie -> "movie"
                MediaType.Tv -> "tv"
                MediaType.Person -> "person"
                MediaType.Unknown -> "unknown"
            }
            "${typeStr}_${target.mediaId}"
        }
    }

    companion object {
        const val REMOTE_KEY_COMMUNITY_LISTS_ENABLED = "remote_community_lists_enabled"
        const val REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED = "remote_discussions_enabled"
        const val REMOTE_KEY_COMMUNITY_REACTIONS_ENABLED = "remote_reactions_enabled"
        const val REMOTE_KEY_DAILY_POLLS_ENABLED = "remote_daily_polls_enabled"
    }
}
