package com.ssverma.showtime.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import coil.imageLoader
import coil.request.ImageRequest
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.showtime.widget.upnext.UpNextGlanceWidget
import com.ssverma.showtime.widget.watchlist.WatchlistGlanceWidget
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class WatchlistWidgetEntry(
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val voteAvg: Float,
    val releaseDate: String,
    val localPosterPath: String?
)

data class UpNextWidgetEntry(
    val showTmdbId: Int,
    val showTitle: String,
    val localPosterPath: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String?
)

object WidgetUpdateHelper {
    private const val TAG = "WidgetUpdateHelper"

    val KEY_WATCHLIST_DATA = stringPreferencesKey("watchlist_data")
    val KEY_UP_NEXT_DATA = stringPreferencesKey("up_next_data")
    val KEY_IS_CONNECTED = booleanPreferencesKey("is_connected")
    val KEY_LAST_UPDATED = longPreferencesKey("last_updated")

    suspend fun updateAllWidgets(context: Context) {
        updateUpNextWidget(context)
        updateWatchlistWidget(context)
    }

    suspend fun updateUpNextWidget(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(UpNextGlanceWidget::class.java)
                if (glanceIds.isEmpty()) return@withContext

                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val accessToken = entryPoint.traktAuthStorage().getAccessToken()
                val isConnected = !accessToken.isNullOrBlank()

                val upNextList: List<TraktUpNextEpisode> =
                    if (isConnected && !accessToken.isNullOrBlank()) {
                        val result = entryPoint.traktSyncRepository().getUpNextQueue(accessToken)
                        result.getOrDefault(emptyList())
                    } else {
                        emptyList()
                    }

                val entries = upNextList.map { episode ->
                    val localPath = episode.showPosterPath?.let { url ->
                        downloadAndCachePoster(context, "tv_${episode.showTmdbId}", url)
                    }
                    UpNextWidgetEntry(
                        showTmdbId = episode.showTmdbId,
                        showTitle = episode.showTitle,
                        localPosterPath = localPath,
                        seasonNumber = episode.seasonNumber,
                        episodeNumber = episode.episodeNumber,
                        episodeTitle = episode.episodeTitle
                    )
                }

                val json = serializeUpNext(entries)

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[KEY_UP_NEXT_DATA] = json
                        prefs[KEY_IS_CONNECTED] = isConnected
                        prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
                    }
                    UpNextGlanceWidget().update(context, glanceId)
                }
                Log.d(
                    TAG,
                    "UpNextGlanceWidget updated with ${entries.size} items for ${glanceIds.size} widgets"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update UpNextGlanceWidget", e)
            }
        }
    }

    suspend fun updateWatchlistWidget(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(WatchlistGlanceWidget::class.java)
                if (glanceIds.isEmpty()) return@withContext

                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                val watchlistItems: List<SavedMediaItem> =
                    entryPoint.libraryRepository().getWatchlistSnapshot()

                val entries = watchlistItems.map { item ->
                    val typeStr =
                        if (item.mediaType == com.ssverma.shared.domain.model.MediaType.Tv) "Tv" else "Movie"
                    val localPath = if (item.posterImageUrl.isNotBlank()) {
                        downloadAndCachePoster(
                            context,
                            "${typeStr}_${item.mediaId}",
                            item.posterImageUrl
                        )
                    } else null

                    WatchlistWidgetEntry(
                        mediaId = item.mediaId,
                        mediaType = typeStr,
                        title = item.title,
                        voteAvg = item.voteAvg,
                        releaseDate = item.releaseDate,
                        localPosterPath = localPath
                    )
                }

                val json = serializeWatchlist(entries)

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[KEY_WATCHLIST_DATA] = json
                        prefs[KEY_LAST_UPDATED] = System.currentTimeMillis()
                    }
                    WatchlistGlanceWidget().update(context, glanceId)
                }
                Log.d(
                    TAG,
                    "WatchlistGlanceWidget updated with ${entries.size} items for ${glanceIds.size} widgets"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update WatchlistGlanceWidget", e)
            }
        }
    }

    private suspend fun downloadAndCachePoster(
        context: Context,
        idPrefix: String,
        url: String
    ): String? {
        return try {
            val cacheDir = File(context.cacheDir, "widget_posters").apply { mkdirs() }
            val file = File(cacheDir, "${idPrefix}.jpg")
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }

            val fullUrl = if (url.startsWith("http")) url else "https://image.tmdb.org/t/p/w200$url"
            val request = ImageRequest.Builder(context)
                .data(fullUrl)
                .size(120, 180)
                .build()
            val result = context.imageLoader.execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun serializeWatchlist(items: List<WatchlistWidgetEntry>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.mediaId)
                put("type", item.mediaType)
                put("title", item.title)
                put("vote", item.voteAvg.toDouble())
                put("date", item.releaseDate)
                put("poster", item.localPosterPath.orEmpty())
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeWatchlist(json: String?): List<WatchlistWidgetEntry> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<WatchlistWidgetEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WatchlistWidgetEntry(
                        mediaId = obj.optInt("id"),
                        mediaType = obj.optString("type", "Movie"),
                        title = obj.optString("title"),
                        voteAvg = obj.optDouble("vote", 0.0).toFloat(),
                        releaseDate = obj.optString("date"),
                        localPosterPath = obj.optString("poster").ifEmpty { null }
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun serializeUpNext(items: List<UpNextWidgetEntry>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("showId", item.showTmdbId)
                put("showTitle", item.showTitle)
                put("poster", item.localPosterPath.orEmpty())
                put("season", item.seasonNumber)
                put("episode", item.episodeNumber)
                put("epTitle", item.episodeTitle.orEmpty())
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeUpNext(json: String?): List<UpNextWidgetEntry> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<UpNextWidgetEntry>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    UpNextWidgetEntry(
                        showTmdbId = obj.optInt("showId"),
                        showTitle = obj.optString("showTitle"),
                        localPosterPath = obj.optString("poster").ifEmpty { null },
                        seasonNumber = obj.optInt("season"),
                        episodeNumber = obj.optInt("episode"),
                        episodeTitle = obj.optString("epTitle").ifEmpty { null }
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }
}
