package com.ssverma.shared.ui.component.media

import androidx.navigation3.runtime.NavKey

data class ShowFeedbackArgs(
    val message: String,
    val actionLabel: String? = null,
    val destination: NavKey? = null
)
