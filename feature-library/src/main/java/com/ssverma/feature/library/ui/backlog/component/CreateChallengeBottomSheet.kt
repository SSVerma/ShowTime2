package com.ssverma.feature.library.ui.backlog.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeBottomSheet(
    selectedTitles: List<ChallengeMediaItem>,
    onOpenSearch: () -> Unit,
    onRemoveSelectedTitle: (ChallengeMediaItem) -> Unit,
    onClearSelectedTitles: () -> Unit,
    onDismiss: () -> Unit,
    onCreateGoal: (
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mediaTypeFilter by remember { mutableStateOf(ChallengeMediaTypeFilter.ALL) }
    var targetCount by remember { mutableFloatStateOf(25f) }

    var showClearAllConfirmation by remember { mutableStateOf(false) }
    var itemPendingRemoval by remember { mutableStateOf<ChallengeMediaItem?>(null) }

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState)
        ) {
            // Header Row (Standard ShowTime Sheet header, no oversized emoji)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.challenges_create_goal_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.challenges_create_goal_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Goal Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.challenges_create_goal_name_label)) },
                placeholder = { Text(stringResource(R.string.challenges_create_goal_name_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description Field (Optional)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.challenges_create_goal_desc_label)) },
                placeholder = { Text(stringResource(R.string.challenges_create_goal_desc_placeholder)) },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media Type Selector
            Text(
                text = stringResource(R.string.challenges_create_goal_media_focus),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                FilterChip(
                    selected = mediaTypeFilter == ChallengeMediaTypeFilter.ALL,
                    onClick = { mediaTypeFilter = ChallengeMediaTypeFilter.ALL },
                    label = { Text(stringResource(R.string.filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = mediaTypeFilter == ChallengeMediaTypeFilter.MOVIE,
                    onClick = { mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.filter_movies)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = mediaTypeFilter == ChallengeMediaTypeFilter.TV,
                    onClick = { mediaTypeFilter = ChallengeMediaTypeFilter.TV },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.filter_tv_shows)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Specific Titles Section
            Text(
                text = stringResource(R.string.challenges_create_goal_titles_label),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.challenges_create_goal_titles_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Trigger Surface (Launches full screen search like Cinema Diary)
            Surface(
                onClick = onOpenSearch,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = stringResource(R.string.challenges_create_goal_search_trigger),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Selected Titles Preview
            if (selectedTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.challenges_create_goal_selected_titles,
                            selectedTitles.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = stringResource(R.string.challenges_create_goal_clear_all),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { showClearAllConfirmation = true }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        selectedTitles,
                        key = { index, item -> "${item.mediaType}_${item.id}_$index" }) { _, item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.width(170.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.size(width = 30.dp, height = 44.dp)
                                ) {
                                    NetworkImage(
                                        url = item.posterImageUrl.convertToTmdbPosterUrl(),
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.releaseYear.isNotBlank()) {
                                        Text(
                                            text = item.releaseYear,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { itemPendingRemoval = item },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Target Count Slider or Summary
            val effectiveTargetCount = maxOf(targetCount.toInt(), selectedTitles.size, 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedTitles.isNotEmpty()) {
                        stringResource(R.string.challenges_create_goal_target_titles)
                    } else {
                        stringResource(R.string.challenges_create_goal_target_count)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.challenges_titles_count, effectiveTargetCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (selectedTitles.isEmpty()) {
                Slider(
                    value = targetCount,
                    onValueChange = { targetCount = it },
                    valueRange = 5f..100f,
                    steps = 18,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Create Goal Button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateGoal(
                            title.trim(),
                            description.trim(),
                            mediaTypeFilter,
                            effectiveTargetCount,
                            selectedTitles
                        )
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = if (selectedTitles.isNotEmpty()) {
                        stringResource(
                            R.string.challenges_create_goal_cta_with_count,
                            selectedTitles.size
                        )
                    } else {
                        stringResource(R.string.challenges_create_goal_cta)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.challenges_clear_all_confirm_title))
            },
            text = {
                Text(
                    stringResource(
                        R.string.challenges_clear_all_confirm_msg,
                        selectedTitles.size
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirmation = false
                        onClearSelectedTitles()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.challenges_create_goal_clear_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmation = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Remove Single Title Confirmation Dialog
    itemPendingRemoval?.let { item ->
        AlertDialog(
            onDismissRequest = { itemPendingRemoval = null },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(stringResource(R.string.challenges_remove_title_confirm_title))
            },
            text = {
                Text(stringResource(R.string.challenges_remove_title_confirm_msg, item.title))
            },
            confirmButton = {
                Button(
                    onClick = {
                        itemPendingRemoval = null
                        onRemoveSelectedTitle(item)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.remove_item))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingRemoval = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
