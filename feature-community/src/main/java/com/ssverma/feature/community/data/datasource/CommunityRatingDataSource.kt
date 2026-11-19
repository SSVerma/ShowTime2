package com.ssverma.feature.community.data.datasource

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.observe
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.analytics.community.FirestoreAuditTracker
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CommunityRatingDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val googleAuthClient: GoogleAuthClient,
    private val firestoreAuditTracker: FirestoreAuditTracker,
    private val ratingSandboxProvider: RatingSandboxProvider,
    keyValueStorageClient: KeyValueStorageClient
) {
    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix get() = if (isDebug) "dev_" else ""
    private val colMediaRatings get() = "${colPrefix}media_ratings"
    private val colUserMediaRatings get() = "${colPrefix}user_media_ratings"

    private val localStorage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "community_ratings_prefs")
    )

    private val optimisticActionFlows =
        ConcurrentHashMap<String, MutableStateFlow<OptimisticRatingAction>>()
    private val latestAggregateCache = ConcurrentHashMap<String, AggregateRatingSnapshot>()
    private val latestRemoteUserRatingCache = ConcurrentHashMap<String, Float>()
    private val mutationLocks = ConcurrentHashMap<String, Mutex>()

    private fun getLocalRatingKey(
        userScopeId: String,
        mediaType: MediaType,
        mediaId: Int
    ): Preferences.Key<Float> {
        return floatPreferencesKey("user_rating_${userScopeId}_${mediaType.toStorageKey()}_$mediaId")
    }

    fun getCommunityRating(mediaType: MediaType, mediaId: Int): Flow<CommunityRating> {
        return ratingSandboxProvider.observeOverride(
            mediaType = mediaType,
            mediaId = mediaId
        ).flatMapLatest { sandboxOverride ->
            if (sandboxOverride != null) {
                flowOf(sandboxOverride)
            } else {
                observeLiveCommunityRating(mediaType = mediaType, mediaId = mediaId)
            }
        }
    }

    private fun observeLiveCommunityRating(
        mediaType: MediaType,
        mediaId: Int
    ): Flow<CommunityRating> {
        val mediaDocKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
        val globalFlow = observeGlobalAggregate(mediaDocKey = mediaDocKey)

        return googleAuthClient.currentUser.flatMapLatest {
            val localScopeId = googleAuthClient.getEffectiveUserId()
            val optimisticKey = "${localScopeId}_$mediaDocKey"
            val optimisticFlow = optimisticActionFlows.getOrPut(optimisticKey) {
                MutableStateFlow(OptimisticRatingAction.None)
            }
            val localKey = getLocalRatingKey(
                userScopeId = localScopeId,
                mediaType = mediaType,
                mediaId = mediaId
            )
            val localUserRatingFlow: Flow<Float?> = localStorage.observe(key = localKey)
                .onStart { emit(null) }
            val remoteUserFlow = observeRemoteUserRating(
                mediaType = mediaType,
                mediaId = mediaId
            )

            combine(
                globalFlow,
                localUserRatingFlow,
                remoteUserFlow,
                optimisticFlow,
                ratingSandboxProvider.observeThreshold()
            ) { remoteAggregate, localUserRating, remoteUserRating, optimisticAction, threshold ->
                CommunityRatingAggregator.resolveDisplayState(
                    mediaType = mediaType,
                    mediaId = mediaId,
                    remoteAggregate = remoteAggregate,
                    localUserRating = localUserRating,
                    remoteUserRating = remoteUserRating,
                    optimisticAction = optimisticAction,
                    sufficientThreshold = threshold
                )
            }
        }
    }

    private fun observeGlobalAggregate(mediaDocKey: String): Flow<AggregateRatingSnapshot> {
        return callbackFlow {
            val queryStartTime = System.currentTimeMillis()
            val docRef = firestore.collection(colMediaRatings).document(mediaDocKey)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                val durationMs = System.currentTimeMillis() - queryStartTime
                if (error != null) {
                    firestoreAuditTracker.trackFirestoreError(
                        queryTag = "ratings_aggregate",
                        throwable = error,
                        durationMs = durationMs
                    )
                    trySend(latestAggregateCache[mediaDocKey] ?: AggregateRatingSnapshot())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    firestoreAuditTracker.trackQuery(
                        queryTag = "ratings_aggregate",
                        screenName = null,
                        docsCount = if (snapshot.exists()) 1 else 0,
                        isFromCache = snapshot.metadata.isFromCache,
                        durationMs = durationMs
                    )
                }

                val parsed = if (snapshot != null && snapshot.exists()) {
                    val rawHistogram = snapshot.get("histogram") as? Map<*, *>
                    val histogram = CommunityRatingAggregator.parseHistogram(rawHistogram)
                    val count = snapshot.getLong("ratingCount")?.toInt() ?: histogram.values.sum()
                    val sum = snapshot.getDouble("ratingSum") ?: 0.0
                    val average = snapshot.getDouble("averageRating")?.toFloat()
                        ?: if (count > 0) (sum / count).toFloat() else 0f
                    AggregateRatingSnapshot(
                        ratingCount = count,
                        ratingSum = sum,
                        averageRating = average,
                        histogram = histogram
                    )
                } else {
                    AggregateRatingSnapshot()
                }

                latestAggregateCache[mediaDocKey] = parsed
                trySend(parsed)
            }
            awaitClose { listener.remove() }
        }.onStart {
            emit(latestAggregateCache[mediaDocKey] ?: AggregateRatingSnapshot())
        }
    }

    private fun observeRemoteUserRating(
        mediaType: MediaType,
        mediaId: Int
    ): Flow<Float?> {
        return callbackFlow {
            val firebaseUid = resolveFirebaseAuthUid()
            if (firebaseUid == null) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }

            val userDocKey = getUserDocKey(
                firebaseUid = firebaseUid,
                mediaType = mediaType,
                mediaId = mediaId
            )
            latestRemoteUserRatingCache[userDocKey]?.let { cached ->
                trySend(cached)
            }
            val docRef = firestore.collection(colUserMediaRatings).document(userDocKey)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val userRating = if (snapshot != null && snapshot.exists()) {
                    snapshot.getDouble("rating")?.toFloat()
                } else {
                    null
                }
                if (userRating != null) {
                    latestRemoteUserRatingCache[userDocKey] = userRating
                } else {
                    latestRemoteUserRatingCache.remove(userDocKey)
                }
                trySend(userRating)
            }
            awaitClose { listener.remove() }
        }.onStart {
            emit(null)
        }
    }

    suspend fun submitRating(
        mediaType: MediaType,
        mediaId: Int,
        rating: Float
    ): Result<CommunityRating, Failure.CoreFailure> {
        val mediaDocKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
        val lock = mutationLocks.getOrPut(mediaDocKey) { Mutex() }

        return lock.withLock {
            val localScopeId = googleAuthClient.getEffectiveUserId()
            val optimisticKey = "${localScopeId}_$mediaDocKey"
            val localKey = getLocalRatingKey(
                userScopeId = localScopeId,
                mediaType = mediaType,
                mediaId = mediaId
            )

            // 1. Persist immediately to user-scoped local DataStore
            localStorage.write(key = localKey, value = rating)

            // 2. Emit 0ms optimistic update for both userRating and aggregate histogram
            val optimisticFlow = optimisticActionFlows.getOrPut(optimisticKey) {
                MutableStateFlow(OptimisticRatingAction.None)
            }
            optimisticFlow.value = OptimisticRatingAction.Submitted(rating = rating)

            val firebaseUid = resolveFirebaseAuthUid()
            val userDocKey = firebaseUid?.let { uid ->
                getUserDocKey(firebaseUid = uid, mediaType = mediaType, mediaId = mediaId)
            }
            val optimisticResult = CommunityRatingAggregator.resolveDisplayState(
                mediaType = mediaType,
                mediaId = mediaId,
                remoteAggregate = latestAggregateCache[mediaDocKey] ?: AggregateRatingSnapshot(),
                localUserRating = rating,
                remoteUserRating = userDocKey?.let { latestRemoteUserRatingCache[it] },
                optimisticAction = OptimisticRatingAction.Submitted(rating = rating),
                sufficientThreshold = CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD
            )

            if (firebaseUid == null || userDocKey == null) {
                return@withLock Result.Success(optimisticResult)
            }

            val globalRef = firestore.collection(colMediaRatings).document(mediaDocKey)
            val userRef = firestore.collection(colUserMediaRatings).document(userDocKey)
            val startTime = System.currentTimeMillis()

            try {
                val updatedAggregate = firestore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)
                    val globalSnapshot = transaction.get(globalRef)

                    val previousRating = userSnapshot.getDouble("rating")?.toFloat()
                    val currentAggregate = AggregateRatingSnapshot(
                        ratingCount = globalSnapshot.getLong("ratingCount")?.toInt() ?: 0,
                        ratingSum = globalSnapshot.getDouble("ratingSum") ?: 0.0,
                        averageRating = globalSnapshot.getDouble("averageRating")?.toFloat() ?: 0f,
                        histogram = CommunityRatingAggregator.parseHistogram(
                            globalSnapshot.get("histogram") as? Map<*, *>
                        )
                    )

                    val nextAggregate = CommunityRatingAggregator.applySubmit(
                        current = currentAggregate,
                        previousRating = previousRating,
                        newRating = rating
                    )

                    val globalData = mapOf(
                        "ratingCount" to nextAggregate.ratingCount,
                        "ratingSum" to nextAggregate.ratingSum,
                        "averageRating" to nextAggregate.averageRating.toDouble(),
                        "histogram" to CommunityRatingAggregator.toFirestoreHistogram(
                            nextAggregate.histogram
                        ),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    transaction.set(globalRef, globalData)

                    val userData = mapOf(
                        "firebaseUid" to firebaseUid,
                        "mediaType" to mediaType.toStorageKey(),
                        "mediaId" to mediaId,
                        "rating" to rating.toDouble(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    transaction.set(userRef, userData)

                    nextAggregate
                }.await()

                latestAggregateCache[mediaDocKey] = updatedAggregate
                latestRemoteUserRatingCache[userDocKey] = rating
                optimisticFlow.value = OptimisticRatingAction.None

                Result.Success(
                    CommunityRating(
                        mediaType = mediaType,
                        mediaId = mediaId,
                        ratingCount = updatedAggregate.ratingCount,
                        ratingSum = updatedAggregate.ratingSum,
                        averageRating = updatedAggregate.averageRating,
                        histogram = updatedAggregate.histogram,
                        userRating = rating
                    )
                )
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                firestoreAuditTracker.trackFirestoreError(
                    queryTag = "ratings_submit",
                    throwable = exception,
                    durationMs = System.currentTimeMillis() - startTime
                )
                Result.Success(optimisticResult)
            }
        }
    }

    suspend fun removeRating(
        mediaType: MediaType,
        mediaId: Int
    ): Result<CommunityRating, Failure.CoreFailure> {
        val mediaDocKey = getMediaDocKey(mediaType = mediaType, mediaId = mediaId)
        val lock = mutationLocks.getOrPut(mediaDocKey) { Mutex() }

        return lock.withLock {
            val localScopeId = googleAuthClient.getEffectiveUserId()
            val optimisticKey = "${localScopeId}_$mediaDocKey"
            val localKey = getLocalRatingKey(
                userScopeId = localScopeId,
                mediaType = mediaType,
                mediaId = mediaId
            )

            // 1. Remove immediately from user-scoped local DataStore
            localStorage.edit { prefs -> prefs.remove(localKey) }

            // 2. Emit 0ms optimistic removal (prevents stale remote rating snap-back)
            val optimisticFlow = optimisticActionFlows.getOrPut(optimisticKey) {
                MutableStateFlow(OptimisticRatingAction.None)
            }
            optimisticFlow.value = OptimisticRatingAction.Removed

            val firebaseUid = resolveFirebaseAuthUid()
            val userDocKey = firebaseUid?.let { uid ->
                getUserDocKey(firebaseUid = uid, mediaType = mediaType, mediaId = mediaId)
            }
            val optimisticResult = CommunityRatingAggregator.resolveDisplayState(
                mediaType = mediaType,
                mediaId = mediaId,
                remoteAggregate = latestAggregateCache[mediaDocKey] ?: AggregateRatingSnapshot(),
                localUserRating = null,
                remoteUserRating = userDocKey?.let { latestRemoteUserRatingCache[it] },
                optimisticAction = OptimisticRatingAction.Removed,
                sufficientThreshold = CommunityRating.MIN_SUFFICIENT_RATINGS_THRESHOLD
            )

            if (firebaseUid == null || userDocKey == null) {
                return@withLock Result.Success(optimisticResult)
            }

            val globalRef = firestore.collection(colMediaRatings).document(mediaDocKey)
            val userRef = firestore.collection(colUserMediaRatings).document(userDocKey)
            val startTime = System.currentTimeMillis()

            try {
                val updatedAggregate = firestore.runTransaction { transaction ->
                    val userSnapshot = transaction.get(userRef)
                    val previousRating = userSnapshot.getDouble("rating")?.toFloat()
                        ?: return@runTransaction null

                    val globalSnapshot = transaction.get(globalRef)
                    val currentAggregate = AggregateRatingSnapshot(
                        ratingCount = globalSnapshot.getLong("ratingCount")?.toInt() ?: 0,
                        ratingSum = globalSnapshot.getDouble("ratingSum") ?: 0.0,
                        averageRating = globalSnapshot.getDouble("averageRating")?.toFloat() ?: 0f,
                        histogram = CommunityRatingAggregator.parseHistogram(
                            globalSnapshot.get("histogram") as? Map<*, *>
                        )
                    )

                    val nextAggregate = CommunityRatingAggregator.applyRemove(
                        current = currentAggregate,
                        previousRating = previousRating
                    )

                    val globalData = mapOf(
                        "ratingCount" to nextAggregate.ratingCount,
                        "ratingSum" to nextAggregate.ratingSum,
                        "averageRating" to nextAggregate.averageRating.toDouble(),
                        "histogram" to CommunityRatingAggregator.toFirestoreHistogram(
                            nextAggregate.histogram
                        ),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    transaction.set(globalRef, globalData)
                    transaction.delete(userRef)

                    nextAggregate
                }.await()

                latestRemoteUserRatingCache.remove(userDocKey)
                if (updatedAggregate != null) {
                    latestAggregateCache[mediaDocKey] = updatedAggregate
                }
                optimisticFlow.value = OptimisticRatingAction.None

                val finalAggregate = updatedAggregate ?: latestAggregateCache[mediaDocKey]
                ?: AggregateRatingSnapshot()
                Result.Success(
                    CommunityRating(
                        mediaType = mediaType,
                        mediaId = mediaId,
                        ratingCount = finalAggregate.ratingCount,
                        ratingSum = finalAggregate.ratingSum,
                        averageRating = finalAggregate.averageRating,
                        histogram = finalAggregate.histogram,
                        userRating = null
                    )
                )
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                firestoreAuditTracker.trackFirestoreError(
                    queryTag = "ratings_remove",
                    throwable = exception,
                    durationMs = System.currentTimeMillis() - startTime
                )
                Result.Success(optimisticResult)
            }
        }
    }

    private suspend fun resolveFirebaseAuthUid(): String? {
        googleAuthClient.currentFirebaseAuthUid?.let { return it }
        googleAuthClient.ensureAuthenticatedSession()
        return googleAuthClient.currentFirebaseAuthUid
    }

    private fun getMediaDocKey(mediaType: MediaType, mediaId: Int): String {
        return "${mediaType.toStorageKey()}_$mediaId"
    }

    private fun getUserDocKey(firebaseUid: String, mediaType: MediaType, mediaId: Int): String {
        return "${firebaseUid}_${mediaType.toStorageKey()}_$mediaId"
    }

    private fun MediaType.toStorageKey(): String = when (this) {
        MediaType.Movie -> "movie"
        MediaType.Tv -> "tv"
        MediaType.Person -> "person"
        MediaType.Unknown -> "unknown"
    }
}
