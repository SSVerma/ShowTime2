package com.ssverma.showtime.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.*
import com.ssverma.shared.ui.component.ClickThroughFilterChip

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NonSelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    ClickThroughFilterChip(
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
        ),
        selected = false
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    ClickThroughFilterChip(
        modifier = modifier,
        elevation = 4.dp,
        selected = true
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun SingleSelectableFilterRow(
    items: List<FilterItem>,
    selectableState: SingleSelectableState<FilterItem>
) {
    SingleSelectableLazyRow(
        items = items,
        selectableState = selectableState,
        itemModifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        selectedItemContent = {
            SelectedFilterChip(text = it.text.asString())
        },
        nonSelectedItemContent = {
            NonSelectedFilterChip(text = it.text.asString())
        }
    )
}

@Composable
fun MultiSelectableFilterRow(
    items: List<FilterItem>,
    selectableState: MultiSelectableState<FilterItem>
) {
    MultiSelectableLazyRow(
        items = items,
        selectableState = selectableState,
        itemModifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        selectedItemContent = {
            SelectedFilterChip(text = it.text.asString())
        },
        nonSelectedItemContent = {
            NonSelectedFilterChip(text = it.text.asString())
        }
    )
}