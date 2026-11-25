package com.ssverma.feature.library.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.community.CommunityListCard
import com.ssverma.core.ui.adaptive.AdaptiveGridDefaults
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.ShowTimeWidthClass
import com.ssverma.core.ui.layout.rememberFloatingBottomBarPadding
import com.ssverma.core.ui.modifier.horizontalWheelScroll
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.ui.home.ExpressiveEmptyState
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityListCategories
import com.ssverma.shared.ui.R as SharedR

/**
 * Displays community curated lists with category filters, adapting cleanly from a single column
 * on Compact viewports to an adaptive multi-column card grid on Medium and Large displays.
 */
@Composable
internal fun CommunityTabContent(
    communityLists: List<CommunityCuratedList>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onCommunityListClick: (CommunityCuratedList) -> Unit,
    onToggleCommunityListUpvote: (String) -> Unit,
    onCloneCommunityList: (CommunityCuratedList) -> Unit,
    onCreateListClick: () -> Unit,
    onMakePrivate: (CommunityCuratedList) -> Unit,
    onDeleteFromCommunity: (CommunityCuratedList) -> Unit,
    onReportCommunityList: (CommunityCuratedList) -> Unit,
    onBlockAuthor: (authorId: String, authorName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactWidth = LocalAdaptiveLayoutInfo.current.widthClass == ShowTimeWidthClass.Compact
    val gridColumns = if (isCompactWidth) {
        GridCells.Fixed(1)
    } else {
        AdaptiveGridDefaults.CommunityCardGridCells
    }
    val cardPosterHeight = if (isCompactWidth) 90.dp else 112.dp
    val categoryListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val contentPadding = rememberFloatingBottomBarPadding(
        start = MaterialTheme.spacing.medium,
        top = MaterialTheme.spacing.medium,
        end = MaterialTheme.spacing.medium,
        extraSpacing = MaterialTheme.spacing.large
    )

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            state = categoryListState,
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.medium,
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalWheelScroll(
                    scrollableState = categoryListState,
                    coroutineScope = coroutineScope
                )
        ) {
            items(
                items = CommunityListCategories.DEFAULT_CATEGORIES,
                key = { category -> category },
                contentType = { "community_category_chip" }
            ) { category ->
                val isSelected = (selectedCategory == category)
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = {
                        Text(
                            text = category,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }

        if (communityLists.isEmpty()) {
            ExpressiveEmptyState(
                title = stringResource(SharedR.string.empty_community_lists_title),
                subtitle = if (selectedCategory == CommunityListCategories.ALL) {
                    stringResource(SharedR.string.empty_community_lists_subtitle)
                } else {
                    stringResource(SharedR.string.no_community_lists_found)
                },
                icon = Icons.Rounded.Public,
                actionButtonText = stringResource(SharedR.string.publish_first_collection),
                onActionClick = onCreateListClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            LazyVerticalGrid(
                columns = gridColumns,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = communityLists,
                    key = { item -> item.listId },
                    contentType = { "community_list_card" }
                ) { item ->
                    CommunityListCard(
                        communityList = item,
                        onClick = { onCommunityListClick(item) },
                        onToggleUpvote = { onToggleCommunityListUpvote(item.listId) },
                        onCloneList = { onCloneCommunityList(item) },
                        posterHeight = cardPosterHeight,
                        onMakePrivate = if (item.isMine) {
                            { onMakePrivate(item) }
                        } else {
                            null
                        },
                        onDeleteFromCommunity = if (item.isMine) {
                            { onDeleteFromCommunity(item) }
                        } else {
                            null
                        },
                        onReport = if (!item.isMine) {
                            { onReportCommunityList(item) }
                        } else {
                            null
                        },
                        onBlockAuthor = if (!item.isMine) {
                            { onBlockAuthor(item.authorId, item.authorName) }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
