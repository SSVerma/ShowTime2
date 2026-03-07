package com.ssverma.feature.person.ui.details.component

import com.ssverma.core.ui.UiText
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.Gender

fun Gender.asUiText(): UiText.StaticText {
    return when (this) {
        Gender.Female -> UiText.StaticText(resId = R.string.female)
        Gender.Male -> UiText.StaticText(resId = R.string.male)
        Gender.Unknown -> UiText.StaticText(resId = R.string.unknown)
    }
}
