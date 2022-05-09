package com.ssverma.showtime.extension

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

fun String?.placeholderIfNullOrEmpty(placeholder: String): String {
    if (this.isNullOrEmpty()) {
        return placeholder
    }
    return this
}

@Composable
fun String?.placeholderIfNullOrEmpty(@StringRes placeholderRes: Int): String {
    return placeholderIfNullOrEmpty(stringResource(id = placeholderRes))
}

fun Float.emptyIfAbsent(): String {
    return if (this == 0F) "" else this.toString()
}

fun Long.emptyIfAbsent(): String {
    return if (this == 0L) "" else this.toString()
}

fun Int.emptyIfAbsent(): String {
    return if (this == 0) "" else this.toString()
}
