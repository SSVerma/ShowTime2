package com.ssverma.showtime.widget.watchlist

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ssverma.showtime.R
import com.ssverma.showtime.widget.WatchlistWidgetEntry
import com.ssverma.showtime.widget.WidgetUpdateHelper
import java.io.File

class WatchlistGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val rawJson = prefs[WidgetUpdateHelper.KEY_WATCHLIST_DATA]
            val items = remember(rawJson) {
                WidgetUpdateHelper.deserializeWatchlist(rawJson)
            }

            GlanceTheme {
                WatchlistWidgetContent(
                    context = context,
                    items = items
                )
            }
        }
    }

    @Composable
    private fun WatchlistWidgetContent(
        context: Context,
        items: List<WatchlistWidgetEntry>
    ) {
        val libraryIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("showtime://www.ssverma.in/library/watchlist")
        ).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF141218)))
                .cornerRadius(18.dp)
                .padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable(actionStartActivity(libraryIntent))
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(ColorProvider(Color(0xFFFFB800)))
                        .cornerRadius(4.dp)
                ) {}

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = context.getString(R.string.widget_watchlist_title),
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                if (items.isNotEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(Color(0xFF332700)))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${items.size}",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFFFC72C)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                // Empty State
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity(libraryIntent))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = context.getString(R.string.widget_no_watchlist),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFE6E1E5)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = context.getString(R.string.widget_watchlist_desc),
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF938F99)),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        val isTv = item.mediaType.equals("tv", ignoreCase = true)
                        val deepLinkPath = if (isTv) "tv" else "movie"
                        val itemIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("showtime://www.ssverma.in/$deepLinkPath/${item.mediaId}")
                        ).apply {
                            setPackage(context.packageName)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }

                        // Guaranteed inter-card spacing wrapper
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .background(ColorProvider(Color(0xFF232128)))
                                    .cornerRadius(12.dp)
                                    .padding(8.dp)
                                    .clickable(actionStartActivity(itemIntent))
                            ) {
                                // Poster Thumbnail (decoded synchronously from local cache)
                                val posterFile = item.localPosterPath?.let { File(it) }
                                val bitmap = if (posterFile != null && posterFile.exists()) {
                                    BitmapFactory.decodeFile(posterFile.absolutePath)
                                } else null

                                if (bitmap != null) {
                                    Image(
                                        provider = ImageProvider(bitmap),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier
                                            .width(36.dp)
                                            .height(50.dp)
                                            .cornerRadius(6.dp)
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = GlanceModifier
                                            .width(36.dp)
                                            .height(50.dp)
                                            .background(ColorProvider(Color(0xFF332F3A)))
                                            .cornerRadius(6.dp)
                                    ) {
                                        Text(
                                            text = if (isTv) "TV" else "🎬",
                                            style = TextStyle(fontSize = 14.sp)
                                        )
                                    }
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                // Text Content Column
                                Column(
                                    modifier = GlanceModifier.defaultWeight()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = GlanceModifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.title,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color.White),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = GlanceModifier.defaultWeight()
                                        )

                                        Spacer(modifier = GlanceModifier.width(6.dp))

                                        // Media Type Badge (TV / MOVIE)
                                        Box(
                                            modifier = GlanceModifier
                                                .background(
                                                    if (isTv) {
                                                        ColorProvider(Color(0xFF381E72))
                                                    } else {
                                                        ColorProvider(Color(0xFF3F3B4A))
                                                    }
                                                )
                                                .cornerRadius(4.dp)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isTv) "TV" else "MOVIE",
                                                style = TextStyle(
                                                    color = ColorProvider(Color(0xFFE8DEF8)),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    if (item.voteAvg > 0f || item.releaseDate.isNotBlank()) {
                                        Spacer(modifier = GlanceModifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (item.voteAvg > 0f) {
                                                Text(
                                                    text = String.format("★ %.1f", item.voteAvg),
                                                    style = TextStyle(
                                                        color = ColorProvider(Color(0xFFFFB800)),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                            }
                                            if (item.releaseDate.isNotBlank()) {
                                                if (item.voteAvg > 0f) {
                                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                                    Text(
                                                        text = "•",
                                                        style = TextStyle(
                                                            color = ColorProvider(Color(0xFF79747E)),
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                                }
                                                Text(
                                                    text = item.releaseDate,
                                                    style = TextStyle(
                                                        color = ColorProvider(Color(0xFFCAC4D0)),
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
