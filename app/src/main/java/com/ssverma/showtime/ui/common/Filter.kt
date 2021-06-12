package com.ssverma.showtime.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.LabelFilter

@Composable
fun NonSelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.56f)
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
fun SelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    DefaultSelectedChip(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onPrimary
        )
    }
}

@Composable
fun <T : LabelFilter> SingleSelectableFilterRow(
    items: List<T>,
    selectableState: SingleSelectableState<T>
) {
    SingleSelectableLazyRow(
        items = items,
        selectableState = selectableState,
        itemModifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        selectedItemContent = {
            SelectedFilterChip(text = it.requireDisplayValue(LocalContext.current))
        },
        nonSelectedItemContent = {
            NonSelectedFilterChip(text = it.requireDisplayValue(LocalContext.current))
        }
    )
}

@Composable
fun <T : LabelFilter> MultiSelectableFilterRow(
    items: List<T>,
    selectableState: MultiSelectableState<T>
) {
    MultiSelectableLazyRow(
        items = items,
        selectableState = selectableState,
        itemModifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        selectedItemContent = {
            SelectedFilterChip(text = it.requireDisplayValue(LocalContext.current))
        },
        nonSelectedItemContent = {
            NonSelectedFilterChip(text = it.requireDisplayValue(LocalContext.current))
        }
    )
}