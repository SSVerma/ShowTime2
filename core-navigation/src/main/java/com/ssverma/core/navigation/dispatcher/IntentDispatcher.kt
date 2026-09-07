package com.ssverma.core.navigation.dispatcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun Context.dispatchImplicitIntent(
    intent: Intent,
    onDestinationNotFound: () -> Unit = {}
) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        onDestinationNotFound()
    }
}

object IntentDispatcher {
    fun Context.dispatchYoutubeIntent(
        videoId: String,
        onNoDestinationFound: () -> Unit = {}
    ) {
        dispatchImplicitIntent(
            intent = CommonIntent.youtubeAppIntent(videoId),
            onDestinationNotFound = {
                dispatchImplicitIntent(
                    intent = CommonIntent.youtubeWebIntent(videoId),
                    onDestinationNotFound = onNoDestinationFound
                )
            }
        )
    }

    fun Context.dispatchBrowserIntent(
        webUrl: String,
        onNoDestinationFound: () -> Unit = {}
    ) {
        dispatchImplicitIntent(
            intent = CommonIntent.browserIntent(webUrl = webUrl),
            onDestinationNotFound = onNoDestinationFound
        )
    }

    fun Context.dispatchShareTextIntent(
        text: String,
        onNoDestinationFound: () -> Unit = {}
    ) {
        dispatchImplicitIntent(
            intent = CommonIntent.shareTextIntent(text = text),
            onDestinationNotFound = onNoDestinationFound
        )
    }

    fun Context.dispatchStreamingIntent(
        watchUrl: String,
        packageName: String? = null,
        onNoDestinationFound: () -> Unit = {}
    ) {
        if (!packageName.isNullOrBlank()) {
            try {
                val deepLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl)).apply {
                    setPackage(packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (deepLinkIntent.resolveActivity(packageManager) != null) {
                    startActivity(deepLinkIntent)
                    return
                }
            } catch (e: Exception) {
                // fall through
            }

            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(launchIntent)
                    return
                }
            } catch (e: Exception) {
                // fall through
            }
        }

        dispatchBrowserIntent(
            webUrl = watchUrl,
            onNoDestinationFound = onNoDestinationFound
        )
    }
}

object CommonIntent {
    fun youtubeAppIntent(youtubeVideoId: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeVideoId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun youtubeWebIntent(youtubeVideoId: String): Intent {
        return browserIntent(webUrl = "https://www.youtube.com/watch?v=$youtubeVideoId")
    }

    fun browserIntent(webUrl: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            webUrl.toUri()
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun shareTextIntent(text: String): Intent {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        return Intent.createChooser(sendIntent, null)
    }
}
