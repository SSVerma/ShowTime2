package com.ssverma.core.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

@Immutable
sealed interface UiText {

    @Immutable
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

    @Immutable
    class PluralText(
        @param:PluralsRes val resId: Int,
        val count: Int,
        vararg val formatArgs: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as PluralText
            if (resId != other.resId || count != other.count) return false
            return formatArgs.contentEquals(other.formatArgs)
        }

        override fun hashCode(): Int {
            var result = resId
            result = 31 * result + count
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    @Immutable
    data class DynamicText(
        val text: String
    ) : UiText

    companion object {
        val Empty: UiText = DynamicText("")
    }
}

fun @receiver:StringRes Int.asUiText(vararg formatArgs: Any): UiText =
    UiText.StaticText(this, *formatArgs)

fun String.asUiText(): UiText =
    UiText.DynamicText(this)

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

        is UiText.PluralText -> {
            val resolvedArgs = formatArgs.map {
                if (it is UiText) it.asString() else it
            }.toTypedArray()
            pluralStringResource(id = this.resId, count = this.count, *resolvedArgs)
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

        is UiText.PluralText -> {
            val resolvedArgs = formatArgs.map {
                if (it is UiText) it.asString(context) else it
            }.toTypedArray()
            context.resources.getQuantityString(this.resId, this.count, *resolvedArgs)
        }
    }
}
