package com.ssverma.feature.search.ui.suggestion.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ssverma.feature.search.R
import com.ssverma.shared.ui.component.illustration.DetailPaneEmptyState

/**
 * Illustrated empty state shown in the secondary pane on wide adaptive screens when Search is
 * open before the user selects a movie, TV show, or person.
 */
@Composable
fun SearchDetailPlaceholder(
    modifier: Modifier = Modifier
) {
    DetailPaneEmptyState(
        title = stringResource(id = R.string.search_detail_placeholder_title),
        subtitle = stringResource(id = R.string.search_detail_placeholder_subtitle),
        modifier = modifier
    )
}
