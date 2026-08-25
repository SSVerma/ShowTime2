package com.ssverma.showtime.widget.upnext

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
import com.ssverma.showtime.MainActivity
import com.ssverma.showtime.R
import com.ssverma.showtime.widget.UpNextWidgetEntry
import com.ssverma.showtime.widget.WidgetUpdateHelper
import java.io.File

class UpNextGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val rawJson = prefs[WidgetUpdateHelper.KEY_UP_NEXT_DATA]
            val isConnected = prefs[WidgetUpdateHelper.KEY_IS_CONNECTED] ?: false
            val items = remember(rawJson) {
                WidgetUpdateHelper.deserializeUpNext(rawJson)
            }

            GlanceTheme {
                UpNextWidgetContent(
                    context = context,
                    items = items,
                    isConnected = isConnected
                )
            }
        }
    }

    @Composable
    private fun UpNextWidgetContent(
        context: Context,
        items: List<UpNextWidgetEntry>,
        isConnected: Boolean
    ) {
        val tvIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("showtime://www.ssverma.in/tv")
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
                    .clickable(actionStartActivity(tvIntent))
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(8.dp)
                        .height(8.dp)
                        .background(ColorProvider(Color(0xFFED1C24)))
                        .cornerRadius(4.dp)
                ) {}

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = context.getString(R.string.widget_up_next_title),
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
                            .background(ColorProvider(Color(0xFF331418)))
                            .cornerRadius(10.dp)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${items.size}",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFFF5252)),
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
                        .clickable(actionStartActivity(tvIntent))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isConnected) {
                                context.getString(R.string.widget_all_caught_up)
                            } else {
                                context.getString(R.string.widget_open_app)
                            },
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFE6E1E5)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = if (isConnected) {
                                context.getString(R.string.widget_up_next_desc)
                            } else {
                                "Connect Trakt in ShowTime to track your queue"
                            },
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
                        val episode = items[index]
                        val showIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("showtime://www.ssverma.in/tv/${episode.showTmdbId}")
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
                                    .clickable(actionStartActivity(showIntent))
                            ) {
                                // Poster Thumbnail (decoded synchronously from local cache)
                                val posterFile = episode.localPosterPath?.let { File(it) }
                                val bitmap = if (posterFile != null && posterFile.exists()) {
                                    BitmapFactory.decodeFile(posterFile.absolutePath)
                                } else null

                                if (bitmap != null) {
                                    Image(
                                        provider = ImageProvider(bitmap),
                                        contentDescription = episode.showTitle,
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
                                            text = "📺",
                                            style = TextStyle(fontSize = 14.sp)
                                        )
                                    }
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                // Text details column
                                Column(
                                    modifier = GlanceModifier.defaultWeight()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = GlanceModifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = episode.showTitle,
                                            maxLines = 1,
                                            style = TextStyle(
                                                color = ColorProvider(Color.White),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = GlanceModifier.defaultWeight()
                                        )

                                        Spacer(modifier = GlanceModifier.width(6.dp))

                                        // Episode Badge (S02E01)
                                        Box(
                                            modifier = GlanceModifier
                                                .background(ColorProvider(Color(0xFF423753)))
                                                .cornerRadius(4.dp)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            val episodeCode = String.format("S%02dE%02d", episode.seasonNumber, episode.episodeNumber)
                                            Text(
                                                text = episodeCode,
                                                style = TextStyle(
                                                    color = ColorProvider(Color(0xFFE8DEF8)),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }

                                    val title = episode.episodeTitle
                                    if (!title.isNullOrBlank()) {
                                        Spacer(modifier = GlanceModifier.height(3.dp))
                                        Text(
                                            text = title,
                                            maxLines = 1,
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
