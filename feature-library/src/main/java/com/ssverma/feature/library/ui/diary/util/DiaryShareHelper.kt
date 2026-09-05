package com.ssverma.feature.library.ui.diary.util

import android.content.Context
import com.ssverma.core.navigation.dispatcher.IntentDispatcher
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.diary.DiaryEntry

object DiaryShareHelper {

    fun shareDiaryEntry(context: Context, entry: DiaryEntry) {
        val shareText = buildString {
            append(entry.title)
            if (entry.releaseDate.isNotBlank()) {
                append(" (${entry.releaseDate.take(4)})")
            }
            append("\n")
            append(context.getString(R.string.diary_share_rating, entry.userRating))
            if (entry.isRewatch) {
                append(" ")
                append(context.getString(R.string.diary_share_rewatch))
            }
            if (entry.review.isNotBlank()) {
                append("\n\n\"")
                append(entry.review)
                append("\"")
            }
            append("\n\n")
            append(context.getString(R.string.diary_share_footer))
        }

        with(IntentDispatcher) {
            context.dispatchShareTextIntent(text = shareText)
        }
    }
}
