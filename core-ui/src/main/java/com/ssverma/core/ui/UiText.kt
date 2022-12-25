package com.ssverma.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    class StaticText(
        @StringRes val resId: Int,
        vararg val formatArgs: Any
    ) : UiText

    data class DynamicText(
        val text: String
    ) : UiText
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicText -> this.text
        is UiText.StaticText -> stringResource(id = this.resId, formatArgs = this.formatArgs)
    }
}