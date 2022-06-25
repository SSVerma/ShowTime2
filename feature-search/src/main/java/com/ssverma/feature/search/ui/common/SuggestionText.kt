package com.ssverma.feature.search.ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun SuggestionText(
    primaryText: String,
    query: String,
    modifier: Modifier = Modifier
) {

    val suggestion = remember(primaryText, query) {
        val queryStartIndex = primaryText.indexOf(query, ignoreCase = true)

        if (queryStartIndex == -1) {
            buildAnnotatedString { append(primaryText) }
        } else {
            val contained = primaryText.substring(queryStartIndex, queryStartIndex + query.length)
            val firstChunk = primaryText.substring(0, queryStartIndex)
            val secondChunk = primaryText.substring(queryStartIndex + query.length)

            buildAnnotatedString {
                append(firstChunk)
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(contained)
                }
                append(secondChunk)
            }
        }
    }

    Text(
        text = suggestion,
        style = MaterialTheme.typography.body1,
        modifier = modifier
    )
}