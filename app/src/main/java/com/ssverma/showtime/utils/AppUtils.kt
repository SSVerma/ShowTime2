package com.ssverma.showtime.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri


object AppUtils {

    fun dispatchOpenYoutubeIntent(context: Context, youtubeVideoId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$youtubeVideoId")).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://www.youtube.com/watch?v=$youtubeVideoId")
        ).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }
}