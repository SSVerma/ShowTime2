package com.ssverma.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShowTimeLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        strokeWidth = ShowTimeLoadingIndicatorDefaults.StrokeWidth,
        modifier = modifier
            .size(ShowTimeLoadingIndicatorDefaults.Size)
    )
}

object ShowTimeLoadingIndicatorDefaults {
    val Size = 24.dp
    val StrokeWidth = 2.dp
}