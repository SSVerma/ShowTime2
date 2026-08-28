package com.ssverma.feature.library.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.core.ui.layout.LocalFloatingBarsVisible
import com.ssverma.core.ui.layout.rememberFloatingBottomBarPadding
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.ReceiptGeneratorHelper
import com.ssverma.feature.library.domain.model.ReceiptItem
import com.ssverma.feature.library.domain.model.ReceiptSource
import com.ssverma.feature.library.domain.model.ReceiptStyle
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.library.ui.home.component.LibraryTab
import com.ssverma.feature.library.ui.home.component.LibraryTabType
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.feature.library.ui.receipt.CinemaReceiptBottomSheet
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.CustomListItem
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.ui.component.media.MediaItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ssverma.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBackPressed: (() -> Unit)? = null,
    isTopLevel: Boolean = onBackPressed == null,
    onMovieClicked: (movieId: Int) -> Unit,
    onTvShowClicked: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    initialTab: LibraryTabDestination = LibraryTabDestination.Watchlist,
    initialMediaType: String? = null,
    targetCustomListId: String? = null,
    viewModel: LibraryHomeViewModel = hiltViewModel()
) {
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val favoriteItems by viewModel.favoriteItems.collectAsState()
    val historyItems by viewModel.historyItems.collectAsState()
    val customLists by viewModel.customLists.collectAsState()

    val watchlistFilter by viewModel.watchlistFilter.collectAsState()
    val favoritesFilter by viewModel.favoritesFilter.collectAsState()
    val historyFilter by viewModel.historyFilter.collectAsState()

    val selectedCustomList by viewModel.selectedCustomList.collectAsState()

    var showCreateListDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var listPendingDeletion by remember { mutableStateOf<CustomList?>(null) }
    var listPendingEdit by remember { mutableStateOf<CustomList?>(null) }
    var topMenuExpanded by remember { mutableStateOf(false) }

    var showReceiptSheet by remember { mutableStateOf(false) }
    var receiptStyle by remember { mutableStateOf(ReceiptStyle.THERMAL) }
    var receiptSource by remember { mutableStateOf(ReceiptSource.HISTORY) }
    var customListForReceipt by remember { mutableStateOf<CustomList?>(null) }

    val activeReceiptSnapshot = remember(
        receiptSource,
        historyItems,
        favoriteItems,
        watchlistItems,
        customListForReceipt
    ) {
        val custom = customListForReceipt
        if (custom != null) {
            val mappedItems = custom.items.map { item ->
                ReceiptItem(
                    id = item.mediaId,
                    title = item.title,
                    year = "",
                    runtimeMinutes = if (item.mediaType == MediaType.Tv) 45 else 115,
                    rating = item.voteAvg
                )
            }
            ReceiptGeneratorHelper.generateSnapshot(
                title = custom.title,
                collectorName = "ShowTime Cinephile",
                items = mappedItems
            )
        } else {
            val itemsToMap = when (receiptSource) {
                ReceiptSource.HISTORY -> historyItems
                ReceiptSource.FAVORITES -> favoriteItems
                ReceiptSource.WATCHLIST -> watchlistItems
                ReceiptSource.THIS_MONTH -> {
                    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    historyItems.filter { it.addedAt >= oneMonthAgo }
                }
            }
            val title = when (receiptSource) {
                ReceiptSource.HISTORY -> "Watch History"
                ReceiptSource.FAVORITES -> "Favorites"
                ReceiptSource.WATCHLIST -> "Watchlist"
                ReceiptSource.THIS_MONTH -> "This Month"
            }
            val mappedItems = itemsToMap.map { item ->
                ReceiptItem(
                    id = item.mediaId,
                    title = item.title,
                    year = item.releaseDate.take(4),
                    runtimeMinutes = if (item.mediaType == MediaType.Tv) 45 else 115,
                    rating = item.voteAvg
                )
            }
            ReceiptGeneratorHelper.generateSnapshot(
                title = title,
                collectorName = "ShowTime Cinephile",
                items = mappedItems
            )
        }
    }

    val tabs = remember(watchlistItems, favoriteItems, historyItems, customLists) {
        listOf(
            LibraryTab(
                title = UiText.StaticText(resId = R.string.watchlist),
                icon = Icons.Rounded.Bookmark,
                tabType = LibraryTabType.Watchlist(items = watchlistItems)
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.favorites),
                icon = Icons.Rounded.Favorite,
                tabType = LibraryTabType.Favorites(items = favoriteItems)
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.watch_history),
                icon = Icons.Rounded.History,
                tabType = LibraryTabType.History(items = historyItems)
            ),
            LibraryTab(
                title = UiText.StaticText(resId = R.string.custom_lists),
                icon = Icons.Rounded.FolderSpecial,
                tabType = LibraryTabType.CustomLists(lists = customLists)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = if (isTopLevel) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    LaunchedEffect(initialTab, initialMediaType, targetCustomListId) {
        val targetPage = when (initialTab) {
            LibraryTabDestination.Watchlist -> 0
            LibraryTabDestination.Favorites -> 1
            LibraryTabDestination.History -> 2
            LibraryTabDestination.CustomLists -> 3
        }
        if (targetPage in 0 until tabs.size) {
            pagerState.animateScrollToPage(targetPage)
        }

        val initialFilter = when (initialMediaType?.lowercase()) {
            "movie" -> MediaTypeFilter.MOVIE
            "tv" -> MediaTypeFilter.TV
            else -> MediaTypeFilter.ALL
        }

        when (initialTab) {
            LibraryTabDestination.Watchlist -> viewModel.setWatchlistFilter(initialFilter)
            LibraryTabDestination.Favorites -> viewModel.setFavoritesFilter(initialFilter)
            LibraryTabDestination.History -> viewModel.setHistoryFilter(initialFilter)
            LibraryTabDestination.CustomLists -> {
                if (targetCustomListId != null) {
                    viewModel.selectCustomList(targetCustomListId)
                }
            }
        }
    }

    AppPage(
        scrollBehavior = scrollBehavior,
        topBar = {
            val isScrolled =
                scrollBehavior.state.collapsedFraction > 0.01f || scrollBehavior.state.contentOffset < -1f
            val headerColor by animateColorAsState(
                targetValue = if (isScrolled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
                animationSpec = tween(durationMillis = 250),
                label = "LibraryHeaderColor"
            )

            val isFloatingBarsVisible = LocalFloatingBarsVisible.current
            val topPadding by animateDpAsState(
                targetValue = if (isFloatingBarsVisible) 64.dp else 0.dp,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                label = "LibraryTopPadding"
            )

            Surface(
                color = headerColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = if (isTopLevel) {
                        Modifier
                            .statusBarsPadding()
                            .padding(top = topPadding)
                    } else {
                        Modifier
                    }
                ) {
                    if (!isTopLevel) {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.library),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                if (onBackPressed != null) {
                                    IconButton(onClick = onBackPressed) {
                                        Icon(
                                            painter = rememberVectorPainter(image = Icons.AutoMirrored.Rounded.ArrowBack),
                                            contentDescription = stringResource(id = CoreUiR.string.back)
                                        )
                                    }
                                }
                            },
                            actions = {
                                if (pagerState.currentPage == 2 && historyItems.isNotEmpty()) {
                                    IconButton(onClick = { topMenuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Rounded.MoreVert,
                                            contentDescription = stringResource(R.string.more_options)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = topMenuExpanded,
                                        onDismissRequest = { topMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.clear_history),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.DeleteOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                topMenuExpanded = false
                                                showClearHistoryDialog = true
                                            }
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    customListForReceipt = null
                                    receiptSource = when (pagerState.currentPage) {
                                        0 -> ReceiptSource.WATCHLIST
                                        1 -> ReceiptSource.FAVORITES
                                        else -> ReceiptSource.HISTORY
                                    }
                                    showReceiptSheet = true
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                        contentDescription = stringResource(R.string.cinema_receipt)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent
                            ),
                            scrollBehavior = scrollBehavior
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            edgePadding = MaterialTheme.spacing.medium,
                            modifier = Modifier.weight(1f),
                            indicator = { tabPositions ->
                                if (pagerState.currentPage < tabPositions.size) {
                                    Box(
                                        modifier = Modifier
                                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                            .height(3.5.dp)
                                            .padding(horizontal = 8.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    topEnd = 4.dp
                                                )
                                            )
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                val selected = pagerState.currentPage == index
                                Tab(
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.primary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.7f
                                    ),
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                val itemCount = tab.itemCount
                                                if (itemCount > 0) {
                                                    Badge(
                                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    ) {
                                                        AnimatedContent(
                                                            targetState = itemCount,
                                                            transitionSpec = {
                                                                if (targetState > initialState) {
                                                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                                                            slideOutVertically { height -> -height } + fadeOut()
                                                                } else {
                                                                    slideInVertically { height -> -height } + fadeIn() togetherWith
                                                                            slideOutVertically { height -> height } + fadeOut()
                                                                }.using(
                                                                    SizeTransform(clip = false)
                                                                )
                                                            },
                                                            label = "TabBadgeCounterAnimation"
                                                        ) { count ->
                                                            Text(
                                                                text = count.toString(),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title.asString()
                                            )
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = tab.title.asString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                )
                            }
                        }

                        if (isTopLevel) {
                            if (pagerState.currentPage == 2 && historyItems.isNotEmpty()) {
                                IconButton(onClick = { topMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = stringResource(R.string.more_options),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = topMenuExpanded,
                                    onDismissRequest = { topMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.clear_history),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            topMenuExpanded = false
                                            showClearHistoryDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) { page ->
            when (page) {
                0 -> {
                    val filteredList = filterItems(watchlistItems, watchlistFilter)
                    MediaCollectionTabContent(
                        items = filteredList,
                        totalCount = watchlistItems.size,
                        movieCount = watchlistItems.count { it.mediaType == MediaType.Movie },
                        tvCount = watchlistItems.count { it.mediaType == MediaType.Tv },
                        activeFilter = watchlistFilter,
                        onFilterSelected = { viewModel.setWatchlistFilter(it) },
                        datePrefix = "Added ",
                        emptyTitle = stringResource(R.string.empty_watchlist_title),
                        emptySubtitle = stringResource(R.string.empty_watchlist_subtitle),
                        emptyIcon = Icons.Rounded.BookmarkBorder,
                        actionIconVector = Icons.Rounded.Bookmark,
                        actionIconTint = MaterialTheme.colorScheme.primary,
                        onItemClick = { item ->
                            if (item.mediaType == MediaType.Tv) onTvShowClicked(
                                item.mediaId
                            ) else onMovieClicked(item.mediaId)
                        },
                        onActionClick = { viewModel.removeFromWatchlist(it.mediaId) },
                        onExploreClick = openSearchPage
                    )
                }

                1 -> {
                    val filteredList = filterItems(favoriteItems, favoritesFilter)
                    MediaCollectionTabContent(
                        items = filteredList,
                        totalCount = favoriteItems.size,
                        movieCount = favoriteItems.count { it.mediaType == MediaType.Movie },
                        tvCount = favoriteItems.count { it.mediaType == MediaType.Tv },
                        activeFilter = favoritesFilter,
                        onFilterSelected = { viewModel.setFavoritesFilter(it) },
                        datePrefix = "Liked ",
                        emptyTitle = stringResource(R.string.empty_favorites_title),
                        emptySubtitle = stringResource(R.string.empty_favorites_subtitle),
                        emptyIcon = Icons.Rounded.FavoriteBorder,
                        actionIconVector = Icons.Rounded.Favorite,
                        actionIconTint = MaterialTheme.colorScheme.error,
                        onItemClick = { item ->
                            if (item.mediaType == MediaType.Tv) onTvShowClicked(
                                item.mediaId
                            ) else onMovieClicked(item.mediaId)
                        },
                        onActionClick = { viewModel.removeFromFavorites(it.mediaId) },
                        onExploreClick = openSearchPage
                    )
                }

                2 -> {
                    val filteredList = filterItems(historyItems, historyFilter)
                    MediaCollectionTabContent(
                        items = filteredList,
                        totalCount = historyItems.size,
                        movieCount = historyItems.count { it.mediaType == MediaType.Movie },
                        tvCount = historyItems.count { it.mediaType == MediaType.Tv },
                        activeFilter = historyFilter,
                        onFilterSelected = { viewModel.setHistoryFilter(it) },
                        datePrefix = "Watched ",
                        emptyTitle = stringResource(R.string.empty_history_title),
                        emptySubtitle = stringResource(R.string.empty_history_subtitle),
                        emptyIcon = Icons.Rounded.History,
                        actionIconVector = Icons.Rounded.Visibility,
                        actionIconTint = MaterialTheme.colorScheme.tertiary,
                        onItemClick = { item ->
                            if (item.mediaType == MediaType.Tv) onTvShowClicked(
                                item.mediaId
                            ) else onMovieClicked(item.mediaId)
                        },
                        onActionClick = { viewModel.removeFromHistory(it.mediaId) },
                        onExploreClick = openSearchPage
                    )
                }

                3 -> {
                    CustomListsHubTabContent(
                        lists = customLists,
                        onCreateListClick = { showCreateListDialog = true },
                        onListClick = { list -> viewModel.selectCustomList(list.listId) },
                        onEditListClick = { list -> listPendingEdit = list },
                        onDeleteListClick = { list -> listPendingDeletion = list },
                        onExploreClick = openSearchPage
                    )
                }
            }
        }
    }

    if (showCreateListDialog) {
        CreateCustomListDialog(
            onDismiss = { showCreateListDialog = false },
            onCreate = { title, desc ->
                viewModel.createCustomList(
                    title,
                    desc
                ); showCreateListDialog = false
            }
        )
    }

    listPendingEdit?.let { list ->
        EditCustomListDialog(
            customList = list,
            onDismiss = { listPendingEdit = null },
            onSave = { title, desc ->
                viewModel.updateCustomList(list.listId, title, desc)
                listPendingEdit = null
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.clear_history),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(text = stringResource(R.string.clear_history_confirm)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearHistory(); showClearHistoryDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(R.string.clear_history))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearHistoryDialog = false
                }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }

    listPendingDeletion?.let { list ->
        AlertDialog(
            onDismissRequest = { listPendingDeletion = null },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_list),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_list_confirm, list.title)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetId = list.listId
                        listPendingDeletion = null
                        if (selectedCustomList?.listId == targetId) {
                            viewModel.selectCustomList(null)
                        }
                        viewModel.deleteCustomList(targetId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(R.string.delete_list))
                }
            },
            dismissButton = {
                TextButton(onClick = { listPendingDeletion = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (selectedCustomList != null) {
        CustomListDetailSheet(
            customList = selectedCustomList!!,
            onDismiss = { viewModel.selectCustomList(null) },
            onItemClick = { item ->
                viewModel.selectCustomList(null)
                if (item.mediaType == MediaType.Tv) onTvShowClicked(item.mediaId) else onMovieClicked(
                    item.mediaId
                )
            },
            onRemoveItem = { item ->
                viewModel.removeItemFromCustomList(
                    selectedCustomList!!.listId,
                    item.mediaId
                )
            },
            onEditList = { listPendingEdit = selectedCustomList },
            onDeleteList = { listPendingDeletion = selectedCustomList },
            onExploreClick = openSearchPage,
            onShareReceipt = {
                customListForReceipt = selectedCustomList
                showReceiptSheet = true
            }
        )
    }

    if (showReceiptSheet) {
        CinemaReceiptBottomSheet(
            snapshot = activeReceiptSnapshot,
            selectedStyle = receiptStyle,
            onStyleSelected = { receiptStyle = it },
            selectedSource = receiptSource,
            onSourceSelected = { receiptSource = it },
            onDismiss = { showReceiptSheet = false },
            isCustomCollection = customListForReceipt != null
        )
    }
}

private fun filterItems(
    items: List<SavedMediaItem>,
    filter: MediaTypeFilter
): List<SavedMediaItem> {
    return when (filter) {
        MediaTypeFilter.ALL -> items
        MediaTypeFilter.MOVIE -> items.filter { it.mediaType == MediaType.Movie }
        MediaTypeFilter.TV -> items.filter { it.mediaType == MediaType.Tv }
    }
}

@Composable
private fun MediaCollectionTabContent(
    items: List<SavedMediaItem>,
    totalCount: Int,
    movieCount: Int,
    tvCount: Int,
    activeFilter: MediaTypeFilter,
    onFilterSelected: (MediaTypeFilter) -> Unit,
    datePrefix: String,
    emptyTitle: String,
    emptySubtitle: String,
    emptyIcon: ImageVector,
    actionIconVector: ImageVector,
    actionIconTint: Color,
    onItemClick: (SavedMediaItem) -> Unit,
    onActionClick: (SavedMediaItem) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var itemPendingRemoval by remember { mutableStateOf<SavedMediaItem?>(null) }

    if (itemPendingRemoval != null) {
        val targetItem = itemPendingRemoval!!
        AlertDialog(
            onDismissRequest = { itemPendingRemoval = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = actionIconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = actionIconVector,
                            contentDescription = null,
                            tint = actionIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.remove_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.remove_dialog_msg, targetItem.title),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onActionClick(targetItem)
                        itemPendingRemoval = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.remove_item),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingRemoval = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    AnimatedContent(
        targetState = totalCount == 0,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        label = "MediaCollectionEmptyOrGrid"
    ) { isEmpty ->
        if (isEmpty) {
            ExpressiveEmptyState(
                title = emptyTitle,
                subtitle = emptySubtitle,
                icon = emptyIcon,
                actionButtonText = stringResource(R.string.explore_titles),
                onActionClick = onExploreClick,
                modifier = modifier
            )
        } else {
            val contentPadding = rememberFloatingBottomBarPadding(
                start = MaterialTheme.spacing.medium,
                top = MaterialTheme.spacing.small,
                end = MaterialTheme.spacing.medium,
                extraSpacing = MaterialTheme.spacing.large
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        FilterChip(
                            selected = activeFilter == MediaTypeFilter.ALL,
                            onClick = { onFilterSelected(MediaTypeFilter.ALL) },
                            label = { Text("${stringResource(R.string.filter_all)} ($totalCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = activeFilter == MediaTypeFilter.MOVIE,
                            onClick = { onFilterSelected(MediaTypeFilter.MOVIE) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("${stringResource(R.string.filter_movies)} ($movieCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = activeFilter == MediaTypeFilter.TV,
                            onClick = { onFilterSelected(MediaTypeFilter.TV) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Tv,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("${stringResource(R.string.filter_tv_shows)} ($tvCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (items.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    vertical = 36.dp,
                                    horizontal = MaterialTheme.spacing.medium
                                )
                        ) {
                            // Multi-layer glowing circular illustration
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    modifier = Modifier.size(80.dp)
                                ) {}
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (activeFilter) {
                                                MediaTypeFilter.MOVIE -> Icons.Rounded.Movie
                                                MediaTypeFilter.TV -> Icons.Rounded.Tv
                                                else -> Icons.Rounded.FilterListOff
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val filterTitle = when (activeFilter) {
                                MediaTypeFilter.MOVIE -> stringResource(R.string.empty_filter_movies_title)
                                MediaTypeFilter.TV -> stringResource(R.string.empty_filter_tv_title)
                                else -> stringResource(R.string.empty_list_items_title)
                            }
                            val filterSubtitle = when (activeFilter) {
                                MediaTypeFilter.MOVIE -> stringResource(R.string.empty_filter_movies_subtitle)
                                MediaTypeFilter.TV -> stringResource(R.string.empty_filter_tv_subtitle)
                                else -> stringResource(R.string.empty_list_items_subtitle)
                            }

                            Text(
                                text = filterTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = filterSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Clean, vertically balanced action hierarchy
                            Button(
                                onClick = { onFilterSelected(MediaTypeFilter.ALL) },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(
                                    horizontal = 24.dp,
                                    vertical = 12.dp
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterListOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${stringResource(R.string.clear_filter)} ($totalCount)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = onExploreClick,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when (activeFilter) {
                                        MediaTypeFilter.MOVIE -> stringResource(R.string.explore_movies)
                                        MediaTypeFilter.TV -> stringResource(R.string.explore_tv_shows)
                                        else -> stringResource(R.string.explore_titles)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = items,
                        key = { "${it.mediaType}_${it.mediaId}" },
                        contentType = { "saved_media" }
                    ) { item ->
                        val badgeScale = remember { Animatable(1f) }
                        val haptic = LocalHapticFeedback.current
                        val coroutineScope = rememberCoroutineScope()

                        MediaItem(
                            title = item.title,
                            posterImageUrl = item.posterImageUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(
                                        durationMillis = 220,
                                        easing = FastOutSlowInEasing
                                    ),
                                    fadeOutSpec = tween(
                                        durationMillis = 180,
                                        easing = FastOutSlowInEasing
                                    ),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ),
                            posterModifier = Modifier.fillMaxWidth(),
                            titleMaxLines = 2,
                            indicator = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    if (item.voteAvg > 0f) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                                alpha = 0.9f
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(
                                                    horizontal = 5.dp,
                                                    vertical = 2.dp
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = String.format("%.1f", item.voteAvg),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    if (activeFilter == MediaTypeFilter.ALL) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                                alpha = 0.9f
                                            )
                                        ) {
                                            Text(
                                                text = if (item.mediaType == MediaType.Tv) "TV" else "Movie",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(
                                                    horizontal = 5.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                    }

                                    if (item.addedAt > 0L) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                                alpha = 0.9f
                                            )
                                        ) {
                                            Text(
                                                text = formatRelativeDate(item.addedAt, datePrefix),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(
                                                    horizontal = 5.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            actionIcon = {
                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        coroutineScope.launch {
                                            badgeScale.animateTo(0.82f, tween(50))
                                            badgeScale.animateTo(
                                                1f,
                                                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                            )
                                        }
                                        itemPendingRemoval = item
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    tonalElevation = 2.dp,
                                    shadowElevation = 3.dp,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .graphicsLayer {
                                            scaleX = badgeScale.value
                                            scaleY = badgeScale.value
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = actionIconVector,
                                            contentDescription = stringResource(R.string.remove_from_library),
                                            tint = actionIconTint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomListsHubTabContent(
    lists: List<CustomList>,
    onCreateListClick: () -> Unit,
    onListClick: (CustomList) -> Unit,
    onEditListClick: (CustomList) -> Unit,
    onDeleteListClick: (CustomList) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = lists.isEmpty(),
        transitionSpec = {
            fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        label = "CustomListsEmptyOrGrid"
    ) { isEmpty ->
        if (isEmpty) {
            ExpressiveEmptyState(
                title = stringResource(R.string.empty_custom_lists_title),
                subtitle = stringResource(R.string.empty_custom_lists_subtitle),
                icon = Icons.Rounded.FolderSpecial,
                actionButtonText = stringResource(R.string.create_custom_list),
                onActionClick = onCreateListClick,
                modifier = modifier
            )
        } else {
            val contentPadding = rememberFloatingBottomBarPadding(
                start = MaterialTheme.spacing.medium,
                top = MaterialTheme.spacing.medium,
                end = MaterialTheme.spacing.medium,
                extraSpacing = MaterialTheme.spacing.large
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = if (lists.size == 1) "1 Collection" else "${lists.size} Collections",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(onClick = onCreateListClick) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.create_custom_list))
                        }
                    }
                }

                items(
                    items = lists,
                    key = { it.listId }
                ) { list ->
                    CustomListCard(
                        customList = list,
                        onClick = { onListClick(list) },
                        onEditClick = { onEditListClick(list) },
                        onDeleteClick = { onDeleteListClick(list) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(
                                    durationMillis = 220,
                                    easing = FastOutSlowInEasing
                                ),
                                fadeOutSpec = tween(
                                    durationMillis = 180,
                                    easing = FastOutSlowInEasing
                                ),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomListCard(
    customList: CustomList,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                val previewPosters = customList.previewPosters
                if (previewPosters.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderSpecial,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        for (i in 0 until minOf(2, previewPosters.size)) {
                            Column(modifier = Modifier.weight(1f)) {
                                NetworkImage(
                                    url = previewPosters[i],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.edit_list),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.delete_list),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(MaterialTheme.spacing.small)) {
                Text(
                    text = customList.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (customList.itemCount == 1) stringResource(R.string.one_item_count) else stringResource(
                        R.string.items_count,
                        customList.itemCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomListDetailSheet(
    customList: CustomList,
    onDismiss: () -> Unit,
    onItemClick: (CustomListItem) -> Unit,
    onRemoveItem: (CustomListItem) -> Unit,
    onEditList: () -> Unit,
    onDeleteList: () -> Unit,
    onExploreClick: () -> Unit,
    onShareReceipt: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var itemPendingRemoval by remember { mutableStateOf<CustomListItem?>(null) }

    if (itemPendingRemoval != null) {
        val targetItem = itemPendingRemoval!!
        AlertDialog(
            onDismissRequest = { itemPendingRemoval = null },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.remove_from_list),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.remove_from_list_confirm, targetItem.title)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveItem(targetItem)
                        itemPendingRemoval = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.remove_item))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemPendingRemoval = null }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customList.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    val desc = customList.description
                    if (!desc.isNullOrBlank()) {
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (customList.itemCount == 1) stringResource(R.string.one_item_count) else stringResource(
                            R.string.items_count,
                            customList.itemCount
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (customList.items.isNotEmpty()) {
                        IconButton(onClick = onShareReceipt) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = stringResource(R.string.cinema_receipt),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onEditList) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_list),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDeleteList) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_list),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            if (customList.items.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = MaterialTheme.spacing.medium)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier.size(80.dp)
                        ) {}
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.FolderSpecial,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.empty_list_items_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.empty_list_items_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onDismiss()
                            onExploreClick()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.explore_and_add_titles),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    items(
                        items = customList.items,
                        key = { "${it.mediaType}_${it.mediaId}" }
                    ) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(
                                        durationMillis = 220,
                                        easing = FastOutSlowInEasing
                                    ),
                                    fadeOutSpec = tween(
                                        durationMillis = 180,
                                        easing = FastOutSlowInEasing
                                    ),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onItemClick(item) }
                                .padding(vertical = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(width = 48.dp, height = 68.dp)
                            ) {
                                NetworkImage(
                                    url = item.posterImageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (item.mediaType == MediaType.Tv) "TV Show" else "Movie",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.voteAvg > 0f) {
                                        Text(
                                            text = " • ★ ${String.format("%.1f", item.voteAvg)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { itemPendingRemoval = item }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.remove_item),
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateCustomListDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.create_custom_list),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.list_title_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.list_desc_hint)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), description.trim().ifEmpty { null })
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.create_list_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun EditCustomListDialog(
    customList: CustomList,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String?) -> Unit
) {
    var title by remember { mutableStateOf(customList.title) }
    var description by remember { mutableStateOf(customList.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.edit_custom_list),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.list_title_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.list_desc_hint)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim().ifEmpty { null })
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.edit_list_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ExpressiveEmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionButtonText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.large,
                    vertical = MaterialTheme.spacing.extraLarge
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(88.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier.size(88.dp)
                    ) {}
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = actionButtonText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun formatRelativeDate(timestamp: Long, prefix: String = "Added "): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - timestamp
    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days == 0L -> "${prefix}Today"
        days == 1L -> "${prefix}Yesterday"
        days in 2L..6L -> "${prefix}${days}d ago"
        days in 7L..29L -> "${prefix}${days / 7}w ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            "$prefix${sdf.format(Date(timestamp))}"
        }
    }
}


