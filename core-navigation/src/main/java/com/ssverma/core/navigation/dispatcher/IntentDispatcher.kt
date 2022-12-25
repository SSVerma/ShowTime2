package com.ssverma.core.navigation.dispatcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.dispatchImplicitIntent(
    intent: Intent,
    onDestinationNotFound: () -> Unit = {}
) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
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
}

object CommonIntent {
    fun youtubeAppIntent(youtubeVideoId: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeVideoId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun youtubeWebIntent(youtubeVideoId: String): Intent {
        return browserIntent(webUrl = "http://www.youtube.com/watch?v=$youtubeVideoId")
    }

    fun browserIntent(webUrl: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(webUrl)
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