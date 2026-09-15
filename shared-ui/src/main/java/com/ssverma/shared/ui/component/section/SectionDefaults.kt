package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object SectionDefaults {
    val SectionVerticalSpacing = 32.dp
    val SectionContentHeaderSpacing = 16.dp
}

fun Modifier.topSectionSpacing(): Modifier =
    this.padding(top = SectionDefaults.SectionVerticalSpacing)