package com.ssverma.feature.match.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.feature.match.R
import com.ssverma.feature.match.ui.component.HandoffPhaseContent
import com.ssverma.feature.match.ui.component.JoinPhaseContent
import com.ssverma.feature.match.ui.component.MatchCelebrationDialog
import com.ssverma.feature.match.ui.component.MatchExitConfirmDialog
import com.ssverma.feature.match.ui.component.MatchQuotaModal
import com.ssverma.feature.match.ui.component.MatchRoomSetupSheet
import com.ssverma.feature.match.ui.component.MatchSummarySheet
import com.ssverma.feature.match.ui.component.MovieSwipeDeck
import com.ssverma.feature.match.ui.component.RemoteLobbyBanner
import com.ssverma.feature.match.ui.component.SetupPhaseContent
import com.ssverma.feature.match.ui.component.SummaryPhaseContent
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoomConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieMatchRoomScreen(
    viewModel: MovieMatchRoomViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (Int) -> Unit,
    openProPaywall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val setupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val summarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSummarySheet by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val handleBack: () -> Unit = {
        when {
            showExitConfirmDialog -> showExitConfirmDialog = false
            uiState.celebratingMatch != null -> viewModel.dismissCelebration()
            uiState.showQuotaModal -> viewModel.dismissQuotaModal()
            showSummarySheet -> showSummarySheet = false
            uiState.showSynopsisSheet != null -> viewModel.closeSynopsis()
            uiState.showSetupSheet -> viewModel.closeSetupSheet()
            uiState.phase == MatchScreenPhase.JOIN -> viewModel.backToSetup()
            uiState.phase == MatchScreenPhase.SWIPING || uiState.phase == MatchScreenPhase.HANDOFF -> {
                showExitConfirmDialog = true
            }

            uiState.phase == MatchScreenPhase.SUMMARY -> viewModel.resetToSetup()
            uiState.phase == MatchScreenPhase.SETUP -> {
                onBackPressed()
            }
        }
    }

    val hasLocalStateToHandle = showExitConfirmDialog ||
            uiState.celebratingMatch != null ||
            uiState.showQuotaModal ||
            showSummarySheet ||
            uiState.showSynopsisSheet != null ||
            uiState.showSetupSheet ||
            uiState.phase != MatchScreenPhase.SETUP

    BackHandler(enabled = isResumed && hasLocalStateToHandle) {
        handleBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.match_room_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.phase == MatchScreenPhase.SWIPING) {
                            Text(
                                text = stringResource(
                                    R.string.match_room_swiping_status,
                                    uiState.activePlayerName,
                                    uiState.topCardIndex + 1,
                                    uiState.cards.size
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.match_room_back)
                        )
                    }
                },
                actions = {
                    if (uiState.phase == MatchScreenPhase.SWIPING) {
                        Surface(
                            onClick = { showSummarySheet = true },
                            shape = CircleShape,
                            color = MovieMatchColor.MatchHeartPink.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                MovieMatchColor.MatchHeartBorder
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = stringResource(R.string.match_room_view_matches_cd),
                                    tint = MovieMatchColor.MatchHeartPink,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.matches.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieMatchColor.MatchHeartPink
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.phase) {
                MatchScreenPhase.SETUP -> {
                    SetupPhaseContent(
                        onStartCouch = {
                            viewModel.startGame(
                                MatchRoomConfig(mode = MatchMode.COUCH),
                                uiState.player1Name,
                                uiState.player2Name
                            )
                        },
                        onOpenCustomize = viewModel::openSetupSheet,
                        onOpenJoin = viewModel::openJoinRoom
                    )
                }

                MatchScreenPhase.JOIN -> {
                    val defaultGuestName = stringResource(R.string.match_room_default_guest)
                    JoinPhaseContent(
                        joinCodeInput = uiState.joinCodeInput,
                        errorMessage = uiState.errorMessage,
                        onCodeChange = viewModel::setJoinCodeInput,
                        onJoin = { viewModel.joinRemoteRoom(guestName = defaultGuestName) },
                        onBackToSetup = viewModel::backToSetup
                    )
                }

                MatchScreenPhase.SWIPING -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.config.mode == MatchMode.REMOTE && uiState.roomCode != null) {
                            RemoteLobbyBanner(
                                roomCode = uiState.roomCode.orEmpty(),
                                partnerName = uiState.partnerName,
                                isGuestConnected = uiState.isGuestConnected,
                                onCopyCode = {
                                    uiState.roomCode?.let { code ->
                                        clipboardManager.setText(AnnotatedString(code))
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.match_room_room_code_copied),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onShareCode = {
                                    uiState.roomCode?.let { code ->
                                        val shareText =
                                            context.getString(R.string.match_room_invite_text, code)
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        context.startActivity(
                                            Intent.createChooser(
                                                intent,
                                                context.getString(R.string.match_room_invite_friend)
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        MovieSwipeDeck(
                            cards = uiState.cards,
                            topCardIndex = uiState.topCardIndex,
                            canRewind = uiState.canUndo,
                            onSwipe = viewModel::onSwipe,
                            onOpenDetails = { openMovieDetails(it.id) },
                            onRewind = viewModel::onRewind,
                            onRewindProPrompt = { viewModel.handleRewindClick(openProPaywall) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }

                MatchScreenPhase.HANDOFF -> {
                    HandoffPhaseContent(
                        nextPlayerName = uiState.player2Name,
                        previousPlayerName = uiState.player1Name,
                        onReady = viewModel::startPlayer2Swiping
                    )
                }

                MatchScreenPhase.SUMMARY -> {
                    SummaryPhaseContent(
                        matches = uiState.matches,
                        savedWatchlistIds = uiState.savedWatchlistIds,
                        onWatchNow = { openMovieDetails(it.id) },
                        onToggleWatchlist = viewModel::toggleWatchlist,
                        onStartNew = viewModel::resetToSetup
                    )
                }
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ShowTimeLoadingIndicator()
                    }
                }
            }

            // Celebration Modal ("It's a Match!")
            uiState.celebratingMatch?.let { match ->
                MatchCelebrationDialog(
                    matchedCard = match,
                    partnerName = uiState.partnerName,
                    isWatchlistAdded = uiState.savedWatchlistIds.contains(match.id),
                    onWatchNow = {
                        viewModel.dismissCelebration()
                        openMovieDetails(match.id)
                    },
                    onAddToWatchlist = {
                        viewModel.toggleWatchlist(match)
                    },
                    onDismiss = viewModel::dismissCelebration
                )
            }

            // Setup Bottom Sheet
            if (uiState.showSetupSheet) {
                MatchRoomSetupSheet(
                    sheetState = setupSheetState,
                    initialConfig = uiState.config,
                    initialPlayer1 = uiState.player1Name,
                    initialPlayer2 = uiState.player2Name,
                    isProOrPassActive = uiState.isProOrPassActive,
                    onStartGame = viewModel::startGame,
                    onOpenJoinRoom = viewModel::openJoinRoom,
                    onUnlockPro = openProPaywall,
                    onDismissRequest = viewModel::closeSetupSheet
                )
            }

            // Summary Bottom Sheet
            if (showSummarySheet) {
                MatchSummarySheet(
                    sheetState = summarySheetState,
                    matches = uiState.matches,
                    savedWatchlistIds = uiState.savedWatchlistIds,
                    onWatchNow = { movie ->
                        showSummarySheet = false
                        openMovieDetails(movie.id)
                    },
                    onToggleWatchlist = viewModel::toggleWatchlist,
                    onStartNewSession = {
                        showSummarySheet = false
                        viewModel.resetToSetup()
                    },
                    onDismissRequest = { showSummarySheet = false }
                )
            }

            // Quota Exceeded Modal
            if (uiState.showQuotaModal) {
                MatchQuotaModal(
                    onUnlockPro = {
                        viewModel.dismissQuotaModal()
                        openProPaywall()
                    },
                    onWatchRewardedAd = { activity ->
                        viewModel.watchRewardedAdForPass(activity)
                    },
                    onDismiss = viewModel::dismissQuotaModal
                )
            }
        }
    }

    if (showExitConfirmDialog) {
        MatchExitConfirmDialog(
            onConfirmExit = {
                showExitConfirmDialog = false
                viewModel.resetToSetup()
            },
            onDismiss = { showExitConfirmDialog = false }
        )
    }
}
