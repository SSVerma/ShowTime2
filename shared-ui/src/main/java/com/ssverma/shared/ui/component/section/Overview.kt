package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.clickable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.ui.R

@Composable
fun OverviewSection(
    overview: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.overview)
            )
        },
        hideIf = overview.isEmpty(),
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        Text(
            text = overview,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Justify,
            maxLines = if (expanded) Int.MAX_VALUE else OverviewMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                expanded = !expanded
            }
        )
    }
}

private const val OverviewMaxLines = 5