package com.ssverma.feature.movie.ui.match

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.ContentScale
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.ui.match.component.MatchCelebrationDialog
import com.ssverma.feature.movie.ui.match.component.MatchRoomSetupSheet
import com.ssverma.feature.movie.ui.match.component.MatchSummarySheet
import com.ssverma.feature.movie.ui.match.component.MovieSwipeDeck
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoomConfig
import com.ssverma.shared.domain.model.match.MovieMatchCard

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
    val setupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val summarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSummarySheet by remember { mutableStateOf(false) }

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
                                text = "Swiping: ${uiState.activePlayerName} • ${uiState.topCardIndex + 1}/${uiState.cards.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.phase == MatchScreenPhase.SWIPING) {
                        // Matches Counter Badge
                        Surface(
                            shape = CircleShape,
                            color = MovieMatchColor.LikeGreen.copy(alpha = 0.2f),
                            border = BorderStroke(
                                1.dp,
                                MovieMatchColor.LikeGreen.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = MovieMatchColor.LikeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.matches.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieMatchColor.LikeGreen
                                )
                            }
                        }

                        // Room Share button (if Remote Mode)
                        uiState.roomCode?.let { code ->
                            IconButton(
                                onClick = {
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
                                            "Invite Friend"
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Share Room Code"
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
                        uiState = uiState,
                        onStartCouch = {
                            viewModel.startGame(
                                MatchRoomConfig(mode = MatchMode.COUCH),
                                uiState.player1Name,
                                uiState.player2Name
                            )
                        },
                        onOpenCustomize = viewModel::openSetupSheet,
                        onOpenJoin = {
                            viewModel.setJoinCodeInput("")
                            viewModel.openSetupSheet()
                        }
                    )
                }

                MatchScreenPhase.JOIN -> {
                    JoinPhaseContent(
                        uiState = uiState,
                        onCodeChange = viewModel::setJoinCodeInput,
                        onJoin = { viewModel.joinRemoteRoom(guestName = "Guest") }
                    )
                }

                MatchScreenPhase.SWIPING -> {
                    MovieSwipeDeck(
                        cards = uiState.cards,
                        topCardIndex = uiState.topCardIndex,
                        canRewind = uiState.canUndo,
                        onSwipe = viewModel::onSwipe,
                        onOpenDetails = { openMovieDetails(it.id) },
                        onRewind = viewModel::onRewind,
                        modifier = Modifier.fillMaxSize()
                    )
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
                        onWatchNow = { openMovieDetails(it.id) },
                        onAddToWatchlist = viewModel::saveToWatchlist,
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
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Celebration Modal ("It's a Match!")
            uiState.celebratingMatch?.let { match ->
                MatchCelebrationDialog(
                    matchedCard = match,
                    partnerName = if (uiState.activePlayerIndex == 0) uiState.player2Name else uiState.player1Name,
                    onWatchNow = {
                        viewModel.dismissCelebration()
                        openMovieDetails(match.id)
                    },
                    onAddToWatchlist = {
                        viewModel.saveToWatchlist(match)
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
                    onUnlockPro = openProPaywall,
                    onDismissRequest = viewModel::closeSetupSheet
                )
            }

            // Quota Exceeded Modal
            if (uiState.showQuotaModal) {
                AlertDialog(
                    onDismissRequest = viewModel::dismissQuotaModal,
                    title = {
                        Text(
                            text = stringResource(R.string.match_room_free_quota_exceeded),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(text = stringResource(R.string.match_room_unlock_with_pass))
                    },
                    confirmButton = {
                        Button(onClick = openProPaywall) {
                            Text("Unlock Pro")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = viewModel::grantRewardedPass) {
                            Text("Watch Video (24h Pass)")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SetupPhaseContent(
    uiState: MovieMatchRoomUiState,
    onStartCouch: () -> Unit,
    onOpenCustomize: () -> Unit,
    onOpenJoin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Icon with Gradient Glow
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.size(88.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Movie Match Night",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe right on movies you like. When you and your friend both swipe right, it's a match!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Couch Mode Quick Start Button
        Button(
            onClick = onStartCouch,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Couch Mode (1 Phone)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Customize Deck / Mode Button
        OutlinedButton(
            onClick = onOpenCustomize,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Customize Deck & Filters",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun JoinPhaseContent(
    uiState: MovieMatchRoomUiState,
    onCodeChange: (String) -> Unit,
    onJoin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.QrCode,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Join Match Room",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter the 5-character code shared by your friend",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.joinCodeInput,
            onValueChange = onCodeChange,
            placeholder = { Text("e.g. ST-4829") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onJoin,
            enabled = uiState.joinCodeInput.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp)
        ) {
            Text("Join Room & Swipe", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HandoffPhaseContent(
    nextPlayerName: String,
    previousPlayerName: String,
    onReady: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🛋️",
            fontSize = 54.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hand phone to $nextPlayerName!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "$previousPlayerName has finished their picks. Now it's your turn to swipe! ShowTime will reveal your mutual matches at the end.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onReady,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "I'm Ready to Swipe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SummaryPhaseContent(
    matches: List<MovieMatchCard>,
    onWatchNow: (MovieMatchCard) -> Unit,
    onAddToWatchlist: (MovieMatchCard) -> Unit,
    onStartNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🎬 Match Summary",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (matches.isNotEmpty()) {
                "You both agreed on ${matches.size} movie${if (matches.size == 1) "" else "s"}!"
            } else {
                "No mutual matches this time. Try again with fresh movies!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // List of matches
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(matches.size) { index ->
                val movie = matches[index]
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(54.dp, 80.dp)
                        ) {
                            NetworkImage(
                                url = movie.posterImageUrl,
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            movie.releaseYear?.let { year ->
                                Text(
                                    text = year,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { onWatchNow(movie) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Watch")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartNew,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Start New Round", fontWeight = FontWeight.Bold)
        }
    }
}
