package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.community.CommunityListCard
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.ui.component.media.SeeAllCard
import com.ssverma.shared.ui.R as SharedR
import com.ssverma.showtime.R

fun LazyListScope.curatedCollectionsShelf(
    isCommunitySelected: Boolean,
    isCommunityListsLoading: Boolean,
    communityLists: List<CommunityCuratedList>,
    customLists: List<CustomList>,
    onToggleCategory: (isCommunity: Boolean) -> Unit,
    onCommunityListClick: (CommunityCuratedList) -> Unit,
    onToggleCommunityUpvote: (String) -> Unit,
    onCloneCommunityList: (CommunityCuratedList) -> Unit,
    onCustomListClick: (CustomList) -> Unit,
    onCreateCustomListClick: () -> Unit,
    onBrowseAllCommunityClick: () -> Unit,
    onManageMyListsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "curated_collections_shelf") {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            SectionHeader(
                title = stringResource(id = R.string.curated_collections_shelf_title),
                leadingIcon = Icons.Rounded.Bookmarks,
                leadingIconContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.7f
                ),
                leadingIconTint = MaterialTheme.colorScheme.primary,
                titleTextStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                trailingContent = {
                    Spacer(modifier = Modifier.width(12.dp))
                    CuratedSegmentedSwitcher(
                        isCommunitySelected = isCommunitySelected,
                        onToggle = onToggleCategory
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = isCommunitySelected,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(
                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                            initialOffsetX = { it / 3 }
                        ) + fadeIn(animationSpec = tween(200))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                targetOffsetX = { -it / 3 }
                            ) + fadeOut(animationSpec = tween(150))
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(250, easing = FastOutSlowInEasing),
                            initialOffsetX = { -it / 3 }
                        ) + fadeIn(animationSpec = tween(200))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                targetOffsetX = { it / 3 }
                            ) + fadeOut(animationSpec = tween(150))
                        )
                    }
                },
                label = "CuratedCollectionsSwitcher"
            ) { showCommunity ->
                if (showCommunity) {
                    if (communityLists.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(
                                items = communityLists,
                                key = { _, item -> "community_list_${item.listId}" },
                                contentType = { _, _ -> "community_list_item" }
                            ) { _, item ->
                                CommunityListCard(
                                    communityList = item,
                                    onClick = { onCommunityListClick(item) },
                                    onToggleUpvote = { onToggleCommunityUpvote(item.listId) },
                                    onCloneList = { onCloneCommunityList(item) },
                                    modifier = Modifier.width(295.dp)
                                )
                            }

                            item(
                                key = "browse_all_community_lists",
                                contentType = "see_all_community"
                            ) {
                                SeeAllCard(
                                    title = stringResource(id = R.string.browse_all_community_lists),
                                    onClick = onBrowseAllCommunityClick,
                                    cardWidth = 150.dp,
                                    cardHeight = 240.dp
                                )
                            }
                        }
                    } else if (isCommunityListsLoading) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            ScreenLoadingIndicator()
                        }
                    } else {
                        EmptyCommunityListsShelfCard(
                            onExploreClick = onBrowseAllCommunityClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    if (customLists.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(
                                items = customLists,
                                key = { _, item -> "custom_list_${item.listId}" },
                                contentType = { _, _ -> "custom_list_item" }
                            ) { _, item ->
                                DashboardCustomListCard(
                                    customList = item,
                                    onClick = { onCustomListClick(item) },
                                    modifier = Modifier.width(210.dp)
                                )
                            }

                            item(
                                key = "manage_my_custom_lists",
                                contentType = "see_all_custom"
                            ) {
                                SeeAllCard(
                                    title = stringResource(id = R.string.manage_lists),
                                    onClick = onManageMyListsClick,
                                    cardWidth = 150.dp,
                                    cardHeight = 212.dp
                                )
                            }
                        }
                    } else {
                        EmptyCustomListsShelfCard(
                            onCreateClick = onCreateCustomListClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CuratedSegmentedSwitcher(
    isCommunitySelected: Boolean,
    onToggle: (isCommunity: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = CircleShape
            )
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CuratedSwitcherPill(
                label = stringResource(id = SharedR.string.tab_my_lists),
                isSelected = !isCommunitySelected,
                onClick = { onToggle(false) }
            )

            CuratedSwitcherPill(
                label = stringResource(id = SharedR.string.tab_community),
                isSelected = isCommunitySelected,
                onClick = { onToggle(true) }
            )
        }
    }
}

@Composable
private fun CuratedSwitcherPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "CuratedPillBgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "CuratedPillTextColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(pillBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
