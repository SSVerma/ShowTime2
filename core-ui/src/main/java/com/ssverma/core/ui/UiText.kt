package com.ssverma.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    class StaticText(
        @param:StringRes val resId: Int,
        vararg val formatArgs: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as StaticText
            if (resId != other.resId) return false
            return formatArgs.contentEquals(other.formatArgs)
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    data class DynamicText(
        val text: String
    ) : UiText
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicText -> this.text
        is UiText.StaticText -> {
            val resolvedArgs = formatArgs.map {
                if (it is UiText) it.asString() else it
            }.toTypedArray()
            stringResource(id = this.resId, *resolvedArgs)
        }
    }
}

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicText -> this.text
        is UiText.StaticText -> {
            val resolvedArgs = formatArgs.map {
                if (it is UiText) it.asString(context) else it
            }.toTypedArray()
            context.getString(this.resId, *resolvedArgs)
        }
    }
}
