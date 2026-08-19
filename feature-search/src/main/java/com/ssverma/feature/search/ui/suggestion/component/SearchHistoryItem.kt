package com.ssverma.feature.search.ui.suggestion.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.ui.common.SearchSuggestionDefaults
import com.ssverma.shared.domain.model.MediaType

@Composable
fun SearchHistoryItem(
    history: SearchHistory,
    onClick: () -> Unit,
    onHistoryClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = SearchSuggestionDefaults.VerticalPadding)
                .padding(
                    start = SearchSuggestionDefaults.HorizontalPadding
                )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)

            Text(
                text = history.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = SearchSuggestionDefaults.HorizontalPadding)
            )

            IconButton(onClick = onHistoryClearClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }
    }
}

@Preview
@Composable
private fun SearchPersonItemPreview() {
    SearchHistoryItem(
        history = SearchHistory(
            id = 1,
            name = "Mr Stark",
            mediaType = MediaType.Movie
        ),
        onClick = {},
        onHistoryClearClick = {}
    )
}