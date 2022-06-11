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
}

object CommonIntent {
    fun youtubeAppIntent(youtubeVideoId: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeVideoId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun youtubeWebIntent(youtubeVideoId: String): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$youtubeVideoId")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}