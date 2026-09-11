package com.ssverma.feature.library.ui.share.component

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString

internal fun copyToClipboard(
    composeClipboardManager: ClipboardManager,
    text: String
) {
    composeClipboardManager.setText(AnnotatedString(text))
}
