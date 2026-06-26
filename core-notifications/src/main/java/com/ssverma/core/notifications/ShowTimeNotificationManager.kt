package com.ssverma.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
        imageUrl: String? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Use app icon
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

    /**
     * Call this for manual testing from a debug screen.
     */
    fun showTestNotification() {
        showNotification(
            title = "Test Notification",
            message = "This is a manual test notification from ShowTime."
        )
    }
}
