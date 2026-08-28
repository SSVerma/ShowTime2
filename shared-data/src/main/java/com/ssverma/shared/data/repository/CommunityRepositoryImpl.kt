package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.domain.repository.CommunityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
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

    private val installationIdKey = stringPreferencesKey("community_installation_id")

    // In-memory cache for instant 0ms optimistic updates
    private val optimisticReactionsCache =
        ConcurrentHashMap<String, MutableStateFlow<MediaReactions>>()

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
}
