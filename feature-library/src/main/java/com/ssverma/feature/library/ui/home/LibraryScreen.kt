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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.PublicOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Visibility
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.shared.domain.utils.ShareMediaUtils
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
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
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.core.ui.layout.LocalFloatingBarsVisible
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
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
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityListCategories
import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.model.library.CustomListItem
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.ui.R as SharedR
import com.ssverma.shared.ui.component.community.CommunityListCard
import com.ssverma.shared.ui.component.community.CommunityListDetailSheet
import com.ssverma.shared.ui.component.community.PublishListBottomSheet
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
    val communityLists by viewModel.communityLists.collectAsState()
    val selectedCommunityCategory by viewModel.selectedCommunityCategory.collectAsState()
    val selectedCommunityList by viewModel.selectedCommunityList.collectAsState()

    val watchlistFilter by viewModel.watchlistFilter.collectAsState()
    val favoritesFilter by viewModel.favoritesFilter.collectAsState()
    val historyFilter by viewModel.historyFilter.collectAsState()

    val selectedCustomList by viewModel.selectedCustomList.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateListDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var listPendingDeletion by remember { mutableStateOf<CustomList?>(null) }
    var listPendingEdit by remember { mutableStateOf<CustomList?>(null) }
    var listPendingPublish by remember { mutableStateOf<CustomList?>(null) }
    var listPendingUnpublish by remember { mutableStateOf<CustomList?>(null) }
    var listPendingClone by remember { mutableStateOf<CommunityCuratedList?>(null) }


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

    val tabs = remember(watchlistItems, favoriteItems, historyItems, customLists, communityLists) {
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
                title = UiText.StaticText(resId = SharedR.string.tab_my_lists),
                icon = Icons.Rounded.FolderSpecial,
                tabType = LibraryTabType.CustomLists(lists = customLists)
            ),
            LibraryTab(
                title = UiText.StaticText(resId = SharedR.string.tab_community),
                icon = Icons.Rounded.Public,
                tabType = LibraryTabType.Community(count = communityLists.size)
            )
        )
    }

    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = if (isTopLevel) {
        TopAppBarDefaults.pinnedScrollBehavior()
    } else {
        TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    // Keep ViewModel synced with pager swipes/clicks
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setSelectedTabIndex(pagerState.currentPage)
    }

    var hasHandledInitialArgs by rememberSaveable(
        initialTab,
        initialMediaType,
        targetCustomListId
    ) {
        mutableStateOf(false)
    }

    LaunchedEffect(initialTab, initialMediaType, targetCustomListId) {
        if (!hasHandledInitialArgs) {
            hasHandledInitialArgs = true
            val targetPage = when (initialTab) {
                LibraryTabDestination.Watchlist -> 0
                LibraryTabDestination.Favorites -> 1
                LibraryTabDestination.History -> 2
                LibraryTabDestination.CustomLists -> 3
                LibraryTabDestination.Community -> 4
            }
            if (targetPage in 0 until tabs.size && (targetPage != 0 || targetPage != pagerState.currentPage)) {
                pagerState.scrollToPage(targetPage)
                viewModel.setSelectedTabIndex(targetPage)
            }

            val initialFilter = when (initialMediaType?.lowercase()) {
                "movie" -> MediaTypeFilter.MOVIE
                "tv" -> MediaTypeFilter.TV
                else -> null
            }

            if (initialFilter != null) {
                when (initialTab) {
                    LibraryTabDestination.Watchlist -> viewModel.setWatchlistFilter(initialFilter)
                    LibraryTabDestination.Favorites -> viewModel.setFavoritesFilter(initialFilter)
                    LibraryTabDestination.History -> viewModel.setHistoryFilter(initialFilter)
                    else -> {}
                }
            }

            if (targetCustomListId != null) {
                viewModel.selectCustomList(targetCustomListId)
            }
        }
    }

    AppPage(
        scrollBehavior = scrollBehavior,
        snackbarHost = {
            ShowTimeSnackbarHost(
                hostState = snackbarHostState,
                floatingBottomBar = true
            )
        },
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
                        onExploreClick = openSearchPage,
                        onClearAll = if (historyItems.isNotEmpty()) {
                            { showClearHistoryDialog = true }
                        } else null
                    )
                }

                3 -> {
                    MyListsTabContent(
                        lists = customLists,
                        onCreateListClick = { showCreateListDialog = true },
                        onListClick = { list -> viewModel.selectCustomList(list.listId) },
                        onEditListClick = { list -> listPendingEdit = list },
                        onDeleteListClick = { list -> listPendingDeletion = list },
                        onExploreCommunityClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                            }
                        }
                    )
                }

                4 -> {
                    CommunityTabContent(
                        communityLists = communityLists,
                        selectedCategory = selectedCommunityCategory,
                        onCategorySelect = viewModel::setCommunityCategory,
                        onCommunityListClick = { list -> viewModel.selectCommunityList(list) },
                        onToggleCommunityListUpvote = { listId ->
                            viewModel.toggleCommunityListUpvote(listId)
                        },
                        onCloneCommunityList = { list ->
                            if (list.isMine) {
                                coroutineScope.launch {
                                    snackbarHostState.showImmediateSnackbar(
                                        message = context.getString(SharedR.string.clone_own_list_warning)
                                    )
                                }
                            } else if (list.isClonedByMe) {
                                coroutineScope.launch {
                                    snackbarHostState.showImmediateSnackbar(
                                        message = context.getString(SharedR.string.already_cloned_warning)
                                    )
                                }
                            } else {
                                listPendingClone = list
                            }
                        },
                        onCreateListClick = { showCreateListDialog = true }
                    )
                }
            }
        }

        if (showCreateListDialog) {
            CreateCustomListDialog(
                onDismiss = { showCreateListDialog = false },
                onCreate = { title, desc ->
                    showCreateListDialog = false
                    viewModel.createCustomList(title, desc) { id ->
                        coroutineScope.launch {
                            if (pagerState.currentPage != 3) {
                                pagerState.animateScrollToPage(3)
                            }
                            snackbarHostState.showImmediateSnackbar(
                                message = context.getString(
                                    SharedR.string.list_created_success,
                                    title
                                )
                            )
                        }
                    }
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
                        text = if (list.isPublic) {
                            stringResource(SharedR.string.delete_public_list_confirm, list.title)
                        } else {
                            stringResource(R.string.delete_list_confirm, list.title)
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetId = list.listId
                            val isPub = list.isPublic
                            listPendingDeletion = null
                            if (selectedCustomList?.listId == targetId) {
                                viewModel.selectCustomList(null)
                            }
                            viewModel.deleteCustomList(targetId, isPublic = isPub)
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
                },
                onPublishClick = { listPendingPublish = selectedCustomList },
                onUnpublishClick = { listPendingUnpublish = selectedCustomList }
            )
        }

        listPendingPublish?.let { listToPublish ->
            val publishSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            PublishListBottomSheet(
                customList = listToPublish,
                sheetState = publishSheetState,
                onDismiss = { listPendingPublish = null },
                onPublish = { categoryTag ->
                    listPendingPublish = null
                    viewModel.selectCustomList(null)
                    viewModel.publishCustomList(
                        localList = listToPublish,
                        categoryTag = categoryTag,
                        onPublished = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(4)
                                snackbarHostState.showImmediateSnackbar(
                                    message = context.getString(SharedR.string.list_published_success)
                                )
                            }
                        },
                        onError = { errorMsg ->
                            coroutineScope.launch {
                                snackbarHostState.showImmediateSnackbar(
                                    message = "Publish failed: $errorMsg"
                                )
                            }
                        }
                    )
                }
            )
        }

        listPendingUnpublish?.let { listToUnpublish ->
            AlertDialog(
                onDismissRequest = { listPendingUnpublish = null },
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PublicOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(SharedR.string.unpublish_dialog_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(SharedR.string.unpublish_dialog_msg))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetId = listToUnpublish.listId
                            listPendingUnpublish = null
                            viewModel.unpublishCustomList(
                                listId = targetId,
                                onUnpublished = {
                                    viewModel.selectCommunityList(null)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(3)
                                        snackbarHostState.showImmediateSnackbar(
                                            message = context.getString(SharedR.string.list_made_private_success)
                                        )
                                    }
                                },
                                onError = { errorMsg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showImmediateSnackbar(
                                            message = "Failed: $errorMsg"
                                        )
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = stringResource(SharedR.string.unpublish_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listPendingUnpublish = null }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        listPendingClone?.let { listToClone ->
            AlertDialog(
                onDismissRequest = { listPendingClone = null },
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = stringResource(SharedR.string.clone_dialog_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            SharedR.string.clone_dialog_msg,
                            listToClone.title,
                            listToClone.itemCount
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetList = listToClone
                            listPendingClone = null
                            viewModel.cloneCommunityList(
                                communityList = targetList,
                                onCloned = {
                                    viewModel.selectCommunityList(null)
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(3)
                                        snackbarHostState.showImmediateSnackbar(
                                            message = context.getString(SharedR.string.list_cloned_success)
                                        )
                                    }
                                },
                                onError = { errorMsg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showImmediateSnackbar(
                                            message = "Clone failed: $errorMsg"
                                        )
                                    }
                                }
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = stringResource(SharedR.string.clone_short))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listPendingClone = null }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        selectedCommunityList?.let { communityList ->
            val communitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            CommunityListDetailSheet(
                communityList = communityList,
                sheetState = communitySheetState,
                onDismiss = { viewModel.selectCommunityList(null) },
                onItemClick = { mediaType, mediaId ->
                    viewModel.selectCommunityList(null)
                    if (mediaType == MediaType.Tv) onTvShowClicked(mediaId) else onMovieClicked(
                        mediaId
                    )
                },
                onToggleUpvote = { viewModel.toggleCommunityListUpvote(communityList.listId) },
                onCloneList = {
                    if (communityList.isMine) {
                        coroutineScope.launch {
                            snackbarHostState.showImmediateSnackbar(
                                message = context.getString(SharedR.string.clone_own_list_warning)
                            )
                        }
                    } else if (communityList.isClonedByMe) {
                        coroutineScope.launch {
                            snackbarHostState.showImmediateSnackbar(
                                message = context.getString(SharedR.string.already_cloned_warning)
                            )
                        }
                    } else {
                        listPendingClone = communityList
                    }
                },
                onUnpublish = if (communityList.isMine) {
                    {
                        val target =
                            customLists.firstOrNull { it.listId == communityList.listId }
                                ?: CustomList(
                                    listId = communityList.listId,
                                    title = communityList.title,
                                    description = communityList.description,
                                    isPublic = true,
                                    createdAt = communityList.createdAtEpochMs,
                                    updatedAt = communityList.updatedAtEpochMs
                                )
                        listPendingUnpublish = target
                    }
                } else null
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
    onClearAll: (() -> Unit)? = null,
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            FilterChip(
                                selected = activeFilter == MediaTypeFilter.ALL,
                                onClick = { onFilterSelected(MediaTypeFilter.ALL) },
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.filter_all)} ($totalCount)",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
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
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.filter_movies)} ($movieCount)",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
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
                                label = {
                                    Text(
                                        text = "${stringResource(R.string.filter_tv_shows)} ($tvCount)",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        if (onClearAll != null) {
                            var showClearMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showClearMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = stringResource(R.string.more_options),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showClearMenu,
                                    onDismissRequest = { showClearMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.clear_history),
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Medium
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
                                            showClearMenu = false
                                            onClearAll()
                                        }
                                    )
                                }
                            }
                        }
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
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                )
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
                                                text = formatRelativeDate(
                                                    item.addedAt,
                                                    datePrefix
                                                ),
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
private fun MyListsTabContent(
    lists: List<CustomList>,
    onCreateListClick: () -> Unit,
    onListClick: (CustomList) -> Unit,
    onEditListClick: (CustomList) -> Unit,
    onDeleteListClick: (CustomList) -> Unit,
    onExploreCommunityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentPadding = rememberFloatingBottomBarPadding(
        start = MaterialTheme.spacing.medium,
        top = MaterialTheme.spacing.medium,
        end = MaterialTheme.spacing.medium,
        extraSpacing = MaterialTheme.spacing.large
    )

    AnimatedContent(
        targetState = lists.isEmpty(),
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    220,
                    easing = FastOutSlowInEasing
                )
            ) togetherWith
                    fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        label = "CustomListsEmptyOrGrid",
        modifier = modifier.fillMaxSize()
    ) { isEmpty ->
        if (isEmpty) {
            ExpressiveEmptyState(
                title = stringResource(R.string.empty_custom_lists_title),
                subtitle = stringResource(R.string.empty_custom_lists_subtitle),
                icon = Icons.Rounded.FolderSpecial,
                actionButtonText = stringResource(R.string.create_custom_list),
                onActionClick = onCreateListClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxSize()
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
private fun CommunityTabContent(
    communityLists: List<CommunityCuratedList>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onCommunityListClick: (CommunityCuratedList) -> Unit,
    onToggleCommunityListUpvote: (String) -> Unit,
    onCloneCommunityList: (CommunityCuratedList) -> Unit,
    onCreateListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentPadding = rememberFloatingBottomBarPadding(
        start = MaterialTheme.spacing.medium,
        top = MaterialTheme.spacing.medium,
        end = MaterialTheme.spacing.medium,
        extraSpacing = MaterialTheme.spacing.large
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Category Filter Chips
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.medium,
                vertical = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(CommunityListCategories.DEFAULT_CATEGORIES) { category ->
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
                    )
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
                columns = GridCells.Fixed(1),
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(communityLists, key = { it.listId }) { item ->
                    CommunityListCard(
                        communityList = item,
                        onClick = { onCommunityListClick(item) },
                        onToggleUpvote = { onToggleCommunityListUpvote(item.listId) },
                        onCloneList = { onCloneCommunityList(item) },
                        modifier = Modifier.fillMaxWidth()
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

                if (customList.isPublic) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(SharedR.string.public_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(SharedR.string.private_badge),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
    onShareReceipt: () -> Unit,
    onPublishClick: () -> Unit,
    onUnpublishClick: () -> Unit
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

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            // Header Info Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = customList.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (customList.isPublic) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (customList.isPublic) Icons.Rounded.Public else Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (customList.isPublic) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = stringResource(if (customList.isPublic) SharedR.string.public_collection_badge else SharedR.string.private_collection_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (customList.isPublic) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    val desc = customList.description
                    if (!desc.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (customList.itemCount == 1) stringResource(R.string.one_item_count) else stringResource(
                            R.string.items_count,
                            customList.itemCount
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar with sleek pill surfaces
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (customList.isPublic) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Surface(
                        onClick = {
                            val shareText = ShareMediaUtils.buildShareableListText(
                                listTitle = customList.title,
                                listDescription = customList.description,
                                authorName = "Me",
                                itemTitles = customList.items.map { it.title },
                                appPackageName = context.packageName,
                                listId = customList.listId
                            )
                            context.dispatchShareTextIntent(text = shareText)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(SharedR.string.share),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        onClick = onUnpublishClick,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PublicOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(SharedR.string.unpublish_action),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else if (customList.items.isNotEmpty()) {
                    Surface(
                        onClick = onPublishClick,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(SharedR.string.publish_action),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (customList.items.isNotEmpty()) {
                    Surface(
                        onClick = onShareReceipt,
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = stringResource(R.string.cinema_receipt),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Edit Button
                Surface(
                    onClick = onEditList,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_list),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Delete Button
                Surface(
                    onClick = onDeleteList,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete_list),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
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
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.empty_list_items_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.empty_list_items_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

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
                        .weight(1f, fill = false)
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
                                            text = " • ★ ${
                                                String.format(
                                                    "%.1f",
                                                    item.voteAvg
                                                )
                                            }",
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
    val bottomBarPadding = rememberFloatingBottomBarPadding()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.large)
            .padding(bottom = bottomBarPadding.calculateBottomPadding() / 2)
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
