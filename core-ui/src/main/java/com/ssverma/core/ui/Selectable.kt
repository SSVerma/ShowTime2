package com.ssverma.core.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.reflect.KProperty

@Composable
fun <T> SingleSelectableLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    selected: T? = null,
    itemModifier: Modifier = Modifier,
    selectableState: SingleSelectableState<T> = rememberSelectableState(selected),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    selectedItemContent: @Composable (item: T) -> Unit,
    nonSelectedItemContent: @Composable (item: T) -> Unit,
) {

    LazyRow(modifier = modifier, contentPadding = contentPadding) {
        itemsIndexed(items) { _, item ->
            Toggleable(
                item = item,
                modifier = itemModifier,
                selectableState = selectableState,
                onContent = { selectedItemContent(item) },
                offContent = { nonSelectedItemContent(item) }
            )
        }
    }
}

@Composable
fun <T> MultiSelectableLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    selected: Set<T> = emptySet(),
    itemModifier: Modifier = Modifier,
    selectableState: MultiSelectableState<T> = rememberSelectableState(selected),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    selectedItemContent: @Composable (item: T) -> Unit,
    nonSelectedItemContent: @Composable (item: T) -> Unit,
) {

    LazyRow(modifier = modifier, contentPadding = contentPadding) {
        itemsIndexed(items) { _, item ->
            Toggleable(
                item = item,
                modifier = itemModifier,
                selectableState = selectableState,
                onContent = { selectedItemContent(item) },
                offContent = { nonSelectedItemContent(item) }
            )
        }
    }
}

@Composable
fun Toggleable(
    selected: Boolean,
    onToggleChange: (isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.toggleable(
            value = selected,
            onValueChange = onToggleChange,
            role = Role.Checkbox,
            interactionSource = interactionSource,
            indication = rememberRipple(bounded = false)
        )
    ) {
        content()
    }
}

@Composable
fun <T> Toggleable(
    item: T,
    modifier: Modifier = Modifier,
    selectableState: SelectableState<T> = rememberSelectableState(null),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onContent: @Composable () -> Unit,
    offContent: @Composable () -> Unit,
) {
    Toggleable(
        selected = selectableState.isSelected(item),
        onToggleChange = {
            selectableState.onSelectionChanged(item)
        },
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        if (selectableState.isSelected(item)) {
            onContent()
        } else {
            offContent()
        }
    }
}

@Composable
fun <T> rememberSelectableState(initialSelected: T? = null): SingleSelectableState<T> {
    return remember(initialSelected) {
        SingleSelectableState(initialValue = initialSelected)
    }
}

@Composable
fun <T> rememberSelectableState(initialSelected: Set<T> = emptySet()): MultiSelectableState<T> {
    return remember(initialSelected) {
        MultiSelectableState(initialValue = initialSelected)
    }
}

interface SelectableState<T> {
    fun onSelectionChanged(item: T)
    fun isSelected(item: T): Boolean
}

class SingleSelectableState<T>(
    initialValue: T? = null
) : SelectableState<T> {
    private var selected by mutableStateOf(initialValue)

    override fun onSelectionChanged(item: T) {
        selected = if (selected == item) {
            null
        } else {
            item
        }
    }

    override fun isSelected(item: T): Boolean {
        return item == selected
    }

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): SingleSelectableState<T> {
        return this
    }

    fun selected(): T? {
        return selected
    }
}

class MultiSelectableState<T>(
    initialValue: Set<T> = emptySet()
) : SelectableState<T> {
    private var selectedItems by mutableStateOf(
        value = initialValue.toMutableSet(),
        policy = neverEqualPolicy()
    )

    override fun onSelectionChanged(item: T) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
        selectedItems = selectedItems
    }

    override fun isSelected(item: T): Boolean {
        return selectedItems.contains(item)
    }

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): MultiSelectableState<T> {
        return this
    }

    fun selected(): Set<T> {
        return selectedItems
    }
}