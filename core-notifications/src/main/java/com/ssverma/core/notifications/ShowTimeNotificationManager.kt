package com.ssverma.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowTimeNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID_GENERAL = "general_notifications"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from ShowTime"
            }

            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        title: String?,
        message: String?,
        imageUrl: String? = null,
        deepLink: String? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (!imageUrl.isNullOrBlank()) {
            val bitmap = fetchBitmap(imageUrl)
            if (bitmap != null) {
                builder.setLargeIcon(bitmap)
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                )
            }
        }

        if (!deepLink.isNullOrBlank()) {
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    deepLink.toUri()
                ).apply {
                    setPackage(context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }

                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentIntent(pendingIntent)
            } catch (e: Exception) {
                // Fallback: Just open the app if deep link is malformed
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                builder.setContentIntent(pendingIntent)
            }
        } else {
            // Default: Launch app
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pendingIntent)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun fetchBitmap(url: String): Bitmap? {
        return try {
            runBlocking {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .build()
                val result = context.imageLoader.execute(request)
                (result.drawable as? BitmapDrawable)?.bitmap
            }
        } catch (e: Exception) {
            null
        }
    }
}
