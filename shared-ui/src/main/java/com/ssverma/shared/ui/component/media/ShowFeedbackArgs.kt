package com.ssverma.shared.ui.component.media

import com.ssverma.feature.library.navigation.LibraryHomeNavKey

data class ShowFeedbackArgs(
    val message: String,
    val actionLabel: String? = null,
    val destination: LibraryHomeNavKey? = null
)
