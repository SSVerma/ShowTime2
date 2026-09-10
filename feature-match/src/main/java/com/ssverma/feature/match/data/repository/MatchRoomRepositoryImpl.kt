package com.ssverma.feature.match.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbLogoUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.match.MatchDeckType
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchProviderBadge
import com.ssverma.shared.domain.model.match.MatchRoom
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MatchRoomStatus
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.model.match.SwipeDirection
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.MatchRoomRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MatchRoomRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val tmdbApiService: TmdbApiService,
    private val appConfigRepository: AppConfigRepository,
    private val watchlistDao: WatchlistDao,
    keyValueStorageClient: KeyValueStorageClient
) : MatchRoomRepository {

    private val gson = Gson()
    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "match_room_prefs")
    )

    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix: String = if (isDebug) "dev_" else ""
    private val colMatchRooms: String get() = "${colPrefix}match_rooms"

    private val persistentUserId: String by lazy {
        val storedId = context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
            .getString("persistent_user_uuid", null)
        if (storedId != null) {
            storedId
        } else {
            val newId = UUID.randomUUID().toString()
            context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("persistent_user_uuid", newId)
                .apply()
            newId
        }
    }

    override suspend fun fetchMatchDeck(config: MatchRoomConfig): Result<List<MovieMatchCard>, Failure<*>> {
        val remoteMovies: List<RemoteMovie> = when (config.deckType) {
            MatchDeckType.TRENDING -> {
                when (val response =
                    tmdbApiService.getTrendingMovies(timeWindow = "day", page = 1)) {
                    is ApiResponse.Success -> response.body.results.orEmpty()
                    else -> emptyList()
                }
            }

            MatchDeckType.MY_SUBSCRIPTIONS -> {
                val userSubs = appConfigRepository.userStreamingSubscriptions.first()
                val providerIds = if (userSubs.isNotEmpty()) {
                    userSubs
                } else {
                    setOf(8, 9, 337, 350) // Netflix, Prime Video, Disney+, Apple TV+
                }
                val queryMap = mutableMapOf<String, String>()
                queryMap["with_watch_providers"] = providerIds.joinToString("|")
                queryMap["watch_region"] = "US"
                queryMap["sort_by"] = "popularity.desc"

                when (val response =
                    tmdbApiService.getDiscoveredMovies(queryMap = queryMap, page = 1)) {
                    is ApiResponse.Success -> response.body.results.orEmpty()
                    else -> emptyList()
                }
            }

            MatchDeckType.GENRE -> {
                val queryMap = mutableMapOf<String, String>()
                val genreId = config.genreId ?: 28 // Default to Action
                queryMap["with_genres"] = genreId.toString()
                queryMap["sort_by"] = "popularity.desc"

                when (val response =
                    tmdbApiService.getDiscoveredMovies(queryMap = queryMap, page = 1)) {
                    is ApiResponse.Success -> response.body.results.orEmpty()
                    else -> emptyList()
                }
            }
        }

        if (remoteMovies.isEmpty()) {
            return Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }

        val cards = remoteMovies.take(config.deckSize).map { remote ->
            MovieMatchCard(
                id = remote.id,
                title = remote.title.orEmpty(),
                posterImageUrl = remote.posterPath.convertToTmdbPosterUrl(),
                backdropImageUrl = remote.backdropPath.convertToTmdbBackdropUrl(),
                releaseYear = remote.releaseDate?.take(4),
                voteAvg = remote.voteAvg,
                overview = remote.overview.orEmpty(),
                genreNames = remote.genres?.mapNotNull { it.name }.orEmpty(),
                watchProviders = emptyList(),
                runtime = if (remote.runtime > 0) remote.runtime else null
            )
        }

        return Result.Success(cards)
    }

    override suspend fun createRemoteRoom(
        hostName: String,
        deck: List<MovieMatchCard>
    ): Result<MatchRoom, Failure<*>> {
        val code = generateRoomCode()
        val roomId = normalizeRoomId(code)
        val deckJson = gson.toJson(deck)

        val roomData = hashMapOf(
            "id" to roomId,
            "roomCode" to code,
            "hostUserId" to persistentUserId,
            "hostName" to hostName,
            "guestUserId" to null,
            "guestName" to null,
            "mode" to MatchMode.REMOTE.name,
            "status" to MatchRoomStatus.WAITING_FOR_GUEST.name,
            "deckJson" to deckJson,
            "hostLikes" to emptyList<Int>(),
            "hostPasses" to emptyList<Int>(),
            "guestLikes" to emptyList<Int>(),
            "guestPasses" to emptyList<Int>(),
            "matches" to emptyList<Int>(),
            "createdAtEpochMs" to System.currentTimeMillis()
        )

        return try {
            firestore.collection(colMatchRooms).document(roomId).set(roomData).await()
            Result.Success(
                MatchRoom(
                    id = roomId,
                    roomCode = code,
                    hostUserId = persistentUserId,
                    hostName = hostName,
                    guestUserId = null,
                    guestName = null,
                    mode = MatchMode.REMOTE,
                    status = MatchRoomStatus.WAITING_FOR_GUEST,
                    deckCards = deck,
                    hostLikes = emptyList(),
                    hostPasses = emptyList(),
                    guestLikes = emptyList(),
                    guestPasses = emptyList(),
                    matches = emptyList(),
                    createdAtEpochMs = System.currentTimeMillis()
                )
            )
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun joinRemoteRoom(
        roomCode: String,
        guestName: String
    ): Result<MatchRoom, Failure<*>> {
        val roomId = normalizeRoomId(roomCode)
        return try {
            val snapshot = firestore.collection(colMatchRooms).document(roomId).get().await()
            if (!snapshot.exists()) {
                return Result.Error(Failure.CoreFailure.UnexpectedFailure)
            }

            firestore.collection(colMatchRooms).document(roomId).update(
                mapOf(
                    "guestUserId" to persistentUserId,
                    "guestName" to guestName,
                    "status" to MatchRoomStatus.SWIPING.name
                )
            ).await()

            val updatedSnapshot = firestore.collection(colMatchRooms).document(roomId).get().await()
            val room = parseRoomSnapshot(updatedSnapshot)
            if (room != null) {
                Result.Success(room)
            } else {
                Result.Error(Failure.CoreFailure.UnexpectedFailure)
            }
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override fun observeRemoteRoom(roomId: String): Flow<MatchRoom?> = callbackFlow {
        val docId = normalizeRoomId(roomId)
        val docRef = firestore.collection(colMatchRooms).document(docId)
        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(parseRoomSnapshot(snapshot))
            } else {
                trySend(null)
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun submitRemoteSwipe(
        roomId: String,
        isHost: Boolean,
        movieId: Int,
        direction: SwipeDirection
    ): Result<Unit, Failure<*>> {
        val docId = normalizeRoomId(roomId)
        val docRef = firestore.collection(colMatchRooms).document(docId)

        return try {
            val fieldName = when {
                isHost && direction == SwipeDirection.LIKE -> "hostLikes"
                isHost && direction == SwipeDirection.PASS -> "hostPasses"
                !isHost && direction == SwipeDirection.LIKE -> "guestLikes"
                else -> "guestPasses"
            }

            docRef.update(fieldName, FieldValue.arrayUnion(movieId)).await()

            if (direction == SwipeDirection.LIKE) {
                val currentSnap = docRef.get().await()
                val otherLikes = if (isHost) {
                    currentSnap.get("guestLikes") as? List<*> ?: emptyList<Any>()
                } else {
                    currentSnap.get("hostLikes") as? List<*> ?: emptyList<Any>()
                }
                val otherLikedIds = otherLikes.mapNotNull { (it as? Number)?.toInt() }.toSet()

                if (otherLikedIds.contains(movieId)) {
                    docRef.update("matches", FieldValue.arrayUnion(movieId)).await()
                }
            }

            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun canStartMatchSession(isProActive: Boolean): Boolean {
        if (isProActive) return true
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val lastDate = storage.data.first()[KEY_LAST_SESSION_DATE]
        val count = storage.data.first()[KEY_DAILY_SESSION_COUNT] ?: 0

        return if (lastDate != today) {
            true
        } else {
            count < 1
        }
    }

    override suspend fun recordMatchSessionStarted() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        storage.edit { prefs ->
            val lastDate = prefs[KEY_LAST_SESSION_DATE]
            val currentCount = prefs[KEY_DAILY_SESSION_COUNT] ?: 0
            if (lastDate == today) {
                prefs[KEY_DAILY_SESSION_COUNT] = currentCount + 1
            } else {
                prefs[KEY_LAST_SESSION_DATE] = today
                prefs[KEY_DAILY_SESSION_COUNT] = 1
            }
        }
    }

    override suspend fun saveMatchToWatchlist(card: MovieMatchCard): Result<Unit, Failure<*>> {
        return try {
            watchlistDao.insertWatchlist(
                WatchlistEntity(
                    mediaId = card.id,
                    mediaType = "movie",
                    title = card.title,
                    posterImageUrl = card.posterImageUrl,
                    backdropImageUrl = card.backdropImageUrl,
                    voteAvg = card.voteAvg,
                    releaseDate = card.releaseYear.orEmpty()
                )
            )
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }

    private fun parseRoomSnapshot(snapshot: DocumentSnapshot): MatchRoom? {
        val id = snapshot.getString("id") ?: return null
        val roomCode = snapshot.getString("roomCode") ?: id.uppercase()
        val hostUserId = snapshot.getString("hostUserId").orEmpty()
        val hostName = snapshot.getString("hostName").orEmpty()
        val guestUserId = snapshot.getString("guestUserId")
        val guestName = snapshot.getString("guestName")
        val modeStr = snapshot.getString("mode") ?: MatchMode.REMOTE.name
        val statusStr = snapshot.getString("status") ?: MatchRoomStatus.WAITING_FOR_GUEST.name
        val deckJson = snapshot.getString("deckJson").orEmpty()
        val createdAt = snapshot.getLong("createdAtEpochMs") ?: System.currentTimeMillis()

        val type = object : TypeToken<List<MovieMatchCard>>() {}.type
        val deckCards = try {
            gson.fromJson<List<MovieMatchCard>>(deckJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val hostLikes = (snapshot.get("hostLikes") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()
        val hostPasses = (snapshot.get("hostPasses") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()
        val guestLikes = (snapshot.get("guestLikes") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()
        val guestPasses = (snapshot.get("guestPasses") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()
        val matches = (snapshot.get("matches") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()

        return MatchRoom(
            id = id,
            roomCode = roomCode,
            hostUserId = hostUserId,
            hostName = hostName,
            guestUserId = guestUserId,
            guestName = guestName,
            mode = runCatching { MatchMode.valueOf(modeStr) }.getOrDefault(MatchMode.REMOTE),
            status = runCatching { MatchRoomStatus.valueOf(statusStr) }.getOrDefault(MatchRoomStatus.WAITING_FOR_GUEST),
            deckCards = deckCards,
            hostLikes = hostLikes,
            hostPasses = hostPasses,
            guestLikes = guestLikes,
            guestPasses = guestPasses,
            matches = matches,
            createdAtEpochMs = createdAt
        )
    }

    private fun generateRoomCode(): String {
        val num = Random.nextInt(1000, 9999)
        return "ST-$num"
    }

    companion object {
        private val KEY_LAST_SESSION_DATE = stringPreferencesKey("match_room_last_session_date")
        private val KEY_DAILY_SESSION_COUNT = intPreferencesKey("match_room_daily_session_count")

        fun normalizeRoomId(rawCode: String): String {
            val cleaned = rawCode.trim().lowercase().replace(" ", "").replace("-", "")
            if (cleaned.isBlank()) return ""
            return if (cleaned.startsWith("st")) {
                "st-" + cleaned.removePrefix("st")
            } else {
                "st-$cleaned"
            }
        }
    }
}
