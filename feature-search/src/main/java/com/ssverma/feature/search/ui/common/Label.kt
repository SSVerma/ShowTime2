package com.ssverma.feature.search.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Label(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.overline,
        color = MaterialTheme.colors.primary,
        modifier = modifier
            .border(
                width = SearchLabelStrokeWidth,
                color = MaterialTheme.colors.primary,
                shape = MaterialTheme.shapes.medium
            )
            .padding(vertical = 2.dp, horizontal = 4.dp)
    )
}

private val SearchLabelStrokeWidth = 1.dp