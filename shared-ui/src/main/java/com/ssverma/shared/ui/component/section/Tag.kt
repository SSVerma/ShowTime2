package com.ssverma.shared.ui.component.section

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.ui.R

@Composable
fun TagsSection(
    keywords: List<Keyword>,
    onClick: (keyword: Keyword) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int = R.string.tags,
) {
    HorizontalLazyListSection(
        items = keywords,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = titleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        hideIf = keywords.isEmpty(),
        itemContent = {
            TagItem(keyword = it) {
                onClick(it)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun TagItem(keyword: Keyword, onTagClicked: () -> Unit) {
    SuggestionChip(
        shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp)),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
            borderWidth = 1.dp
        ),
        onClick = onTagClicked,
        label = {
            Text(
                text = "#${keyword.name}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
