package com.ssverma.feature.person.ui.details.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun PersonBiography(
    biography: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }

    Text(
        text = biography,
        textAlign = TextAlign.Start,
        maxLines = if (isExpanded) Int.MAX_VALUE else maxLines,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .animateContentSize()
            .clickable {
                isExpanded = !isExpanded
            }
    )
}
