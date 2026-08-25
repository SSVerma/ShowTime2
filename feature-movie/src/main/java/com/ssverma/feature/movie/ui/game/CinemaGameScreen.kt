package com.ssverma.feature.movie.ui.game

import android.app.Activity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.GameFeedbackParticles
import com.ssverma.core.ui.component.GameParticleType
import com.ssverma.core.ui.component.ScratchCard
import com.ssverma.feature.movie.R
import com.ssverma.shared.domain.model.game.DailyCinemaPuzzle
import com.ssverma.shared.domain.model.game.GameClue
import com.ssverma.shared.domain.model.game.GameClueType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinemaGameScreen(
    viewModel: CinemaGameViewModel,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val activity = context as? Activity

    var particleEffect by remember { mutableStateOf(GameParticleType.SUCCESS_CONFETTI) }
    var particleTriggerKey by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.gameEffectEvent.collectLatest { (effect, triggerKey) ->
            particleEffect = effect
            particleTriggerKey = triggerKey
        }
    }

    // Prefetch all clue & poster images into Coil cache ahead of time
    LaunchedEffect(uiState.puzzle) {
        uiState.puzzle?.let { puzzle ->
            puzzle.clues.forEach { clue ->
                clue.imageUrl?.let { url ->
                    val request = ImageRequest.Builder(context)
                        .data(url)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build()
                    context.imageLoader.enqueue(request)
                }
            }
            if (puzzle.posterImageUrl.isNotBlank()) {
                val request = ImageRequest.Builder(context)
                    .data(puzzle.posterImageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    // Smooth scroll when clue advances
    LaunchedEffect(uiState.selectedClueIndex) {
        if (uiState.selectedClueIndex > 0) {
            listState.animateScrollToItem(1)
        }
    }

    // Auto-scroll to top on new attempt
    LaunchedEffect(uiState.attemptNumber) {
        listState.scrollToItem(0)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.cinema_challenge_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState.isBonusReel) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFF9800).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.cinema_challenge_bonus_reel_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9800),
                                            modifier = Modifier.padding(
                                                horizontal = 5.dp,
                                                vertical = 1.5.dp
                                            )
                                        )
                                    }
                                }
                            }
                            uiState.puzzle?.let {
                                Text(
                                    text = stringResource(
                                        id = R.string.cinema_challenge_puzzle_number,
                                        it.puzzleNumber,
                                        if (uiState.isBonusReel) "B" else ""
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPress) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    actions = {
                        // Streak Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = stringResource(id = R.string.cinema_stats_streak),
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFFF9800)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${uiState.stats.currentStreak}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }

                        // Stats button
                        IconButton(onClick = { viewModel.setShowStatsDialog(true) }) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = stringResource(id = R.string.cinema_stats_title),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val puzzle = uiState.puzzle
                if (puzzle == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(id = R.string.cinema_challenge_error_loading))
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .imePadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(2.dp))

                            // 5-Slot Guess Tracker Header
                            GuessProgressTracker(
                                submittedGuesses = uiState.submittedGuesses,
                                targetTitle = puzzle.targetMovieTitle,
                                maxGuesses = 5
                            )
                        }

                        item {
                            // Horizontal Clue Selector Chips (1 to 5)
                            ClueSelectorRow(
                                clues = puzzle.clues,
                                unlockedIndex = uiState.unlockedClueIndex,
                                selectedIndex = uiState.selectedClueIndex,
                                onClueSelect = { viewModel.selectClue(it) }
                            )
                        }

                        item {
                            // Active Clue Card
                            val activeClue = puzzle.clues.getOrNull(uiState.selectedClueIndex)
                                ?: puzzle.clues.first()

                            ClueCard(
                                clue = activeClue,
                                isFirstClue = uiState.selectedClueIndex == 0,
                                isGameOver = uiState.isGameOver
                            )
                        }

                        item {
                            // Previous Guesses History List
                            if (uiState.submittedGuesses.isNotEmpty()) {
                                GuessHistoryList(
                                    guesses = uiState.submittedGuesses,
                                    targetTitle = puzzle.targetMovieTitle
                                )
                            }
                        }

                        item {
                            // Guess Input with Autocomplete & Keyboard Action or Game Over / Second Chance Banner
                            if (!uiState.isGameOver) {
                                GuessInputSection(
                                    searchQuery = uiState.searchQuery,
                                    isSearching = uiState.isSearching,
                                    suggestions = uiState.searchSuggestions,
                                    remainingGuesses = uiState.remainingGuesses,
                                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onSelectSuggestion = { viewModel.submitGuess(it) },
                                    onSubmitGuess = { viewModel.submitGuess(uiState.searchQuery) },
                                    onSkip = { viewModel.skipClue() }
                                )
                            } else {
                                GameOverBanner(
                                    gameStatus = uiState.gameStatus,
                                    puzzle = puzzle,
                                    canUnlockSecondChance = uiState.canUnlockSecondChance,
                                    isUnlockingSecondChance = uiState.isUnlockingSecondChance,
                                    onUnlockSecondChance = {
                                        activity?.let { viewModel.unlockSecondChanceBonusReel(it) }
                                    },
                                    onOpenStats = { viewModel.setShowStatsDialog(true) }
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // Multi-effect particle animation (Confetti on win, embers on wrong guess, cascade on game over)
        GameFeedbackParticles(
            effectType = particleEffect,
            triggerKey = particleTriggerKey,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Stats Dialog
    if (uiState.showStatsDialog) {
        CinemaGameStatsDialog(
            stats = uiState.stats,
            puzzle = uiState.puzzle,
            gameStatus = uiState.gameStatus,
            guessCount = uiState.submittedGuesses.size,
            shareableText = uiState.shareableText,
            isBonusReel = uiState.isBonusReel,
            canUnlockSecondChance = uiState.canUnlockSecondChance,
            onUnlockSecondChance = {
                activity?.let { viewModel.unlockSecondChanceBonusReel(it) }
            },
            onDismiss = { viewModel.setShowStatsDialog(false) }
        )
    }

    // Bonus Reel Golden Scratch Ticket
    if (uiState.showBonusReelScratch) {
        BonusReelScratchDialog(
            onDismiss = { viewModel.onBonusReelScratched() }
        )
    }
}

@Composable
private fun ClueSelectorRow(
    clues: List<GameClue>,
    unlockedIndex: Int,
    selectedIndex: Int,
    onClueSelect: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(clues) { index, clue ->
            val isUnlocked = index <= unlockedIndex
            val isSelected = index == selectedIndex

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isUnlocked -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) { onClueSelect(index) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isUnlocked) {
                        Text(
                            text = stringResource(
                                id = R.string.cinema_challenge_clue_tab,
                                index + 1
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = stringResource(
                                id = R.string.cinema_challenge_clue_tab,
                                index + 1
                            ),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                id = R.string.cinema_challenge_clue_tab,
                                index + 1
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClueCard(
    clue: GameClue,
    isFirstClue: Boolean,
    isGameOver: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = clue.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = stringResource(
                        id = R.string.cinema_challenge_clue_n_of_5,
                        clue.clueNumber
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val imageUrl = clue.imageUrl
            if (!imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF181818))
                ) {
                    if (isFirstClue && !isGameOver) {
                        ScratchCard(
                            modifier = Modifier.fillMaxSize(),
                            enabled = true,
                            scratchThresholdFraction = 0.30f,
                            overlayColor = Color(0xFF1F1F23),
                            brushStrokeWidth = 90f,
                            overlayContent = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Movie,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stringResource(id = R.string.cinema_challenge_visual_mystery_title),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.cinema_challenge_scratch_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            revealedContent = {
                                NetworkImage(
                                    url = imageUrl,
                                    contentDescription = clue.label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        )
                    } else {
                        NetworkImage(
                            url = imageUrl,
                            contentDescription = clue.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = clue.content,
                style = if (clue.type == GameClueType.PLOT_TAGLINE)
                    MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic)
                else MaterialTheme.typography.bodyLarge,
                fontWeight = if (clue.type == GameClueType.CAST_DIRECTOR || clue.type == GameClueType.RELEASE_YEAR)
                    FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GuessProgressTracker(
    submittedGuesses: List<String>,
    targetTitle: String,
    maxGuesses: Int = 5
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0 until maxGuesses) {
            val guess = submittedGuesses.getOrNull(i)
            val isCurrent = i == submittedGuesses.size

            val (bgColor, borderColor) = when {
                guess != null && isMatchingTitleNormalized(guess, targetTitle) ->
                    Color(0xFF4CAF50) to Color(0xFF388E3C)

                guess != null && guess.equals("Skipped", ignoreCase = true) ->
                    Color(0xFF78909C) to Color(0xFF546E7A)

                guess != null ->
                    MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error

                isCurrent ->
                    MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary

                else ->
                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f) to MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = 0.25f
                    )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .border(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun GuessHistoryList(
    guesses: List<String>,
    targetTitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        guesses.forEachIndexed { index, guess ->
            val isCorrect = isMatchingTitleNormalized(guess, targetTitle)
            val isSkipped = guess.equals("Skipped", ignoreCase = true)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    isSkipped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = when {
                            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                            isSkipped -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isCorrect -> Icons.Rounded.CheckCircle
                            isSkipped -> Icons.Rounded.FastForward
                            else -> Icons.Rounded.Cancel
                        },
                        contentDescription = null,
                        tint = when {
                            isCorrect -> Color(0xFF4CAF50)
                            isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isSkipped) "Skipped Clue ${index + 1}" else guess,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GuessInputSection(
    searchQuery: String,
    isSearching: Boolean,
    suggestions: List<RemoteMultiSearchSuggestion>,
    remainingGuesses: Int,
    onQueryChange: (String) -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onSubmitGuess: () -> Unit,
    onSkip: () -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(suggestions.isNotEmpty(), searchQuery) {
        if (suggestions.isNotEmpty() || searchQuery.isNotBlank()) {
            bringIntoViewRequester.bringIntoView()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        // Full Width Search Input with 1-Tap Autocomplete and Keyboard Submit
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(id = R.string.cinema_challenge_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.back),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onSubmitGuess()
                    }
                }
            ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            bringIntoViewRequester.bringIntoView()
                        }
                    }
                }
        )

        // Suggestions Dropdown List (Tap immediately submits guess)
        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    suggestions.forEach { suggestion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSelectSuggestion(suggestion.name.orEmpty())
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = suggestion.name.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skip Clue Button
        OutlinedButton(
            onClick = onSkip,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = R.string.cinema_challenge_skip_clue, remainingGuesses))
        }
    }
}

@Composable
private fun GameOverBanner(
    gameStatus: GameStatus,
    puzzle: DailyCinemaPuzzle,
    canUnlockSecondChance: Boolean,
    isUnlockingSecondChance: Boolean,
    onUnlockSecondChance: () -> Unit,
    onOpenStats: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gameStatus == GameStatus.WON)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else if (canUnlockSecondChance)
                MaterialTheme.colorScheme.surfaceContainerHigh
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (gameStatus == GameStatus.WON) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.cinema_challenge_won_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(
                        id = R.string.cinema_challenge_won_desc,
                        puzzle.targetMovieTitle,
                        puzzle.releaseYear
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenStats,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.cinema_challenge_view_result_share),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (canUnlockSecondChance) {
                // Second Chance Option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.cinema_challenge_second_chance_tag),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(id = R.string.cinema_challenge_missed_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(id = R.string.cinema_challenge_missed_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onUnlockSecondChance,
                    enabled = !isUnlockingSecondChance,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6D00)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isUnlockingSecondChance) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(id = R.string.cinema_challenge_watch_bonus_reel_action),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenStats,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(stringResource(id = R.string.cinema_challenge_reveal_answer_finish))
                }
            } else {
                Text(
                    text = stringResource(id = R.string.cinema_challenge_today_challenge_complete),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(
                        id = R.string.cinema_challenge_answer_reveal,
                        puzzle.targetMovieTitle,
                        puzzle.releaseYear
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenStats,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.cinema_challenge_view_final_result_share),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BonusReelScratchDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1700)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.cinema_challenge_bonus_reel_scratch_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(id = R.string.cinema_challenge_bonus_reel_scratch_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Golden Scratch Ticket
                ScratchCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    scratchThresholdFraction = 0.30f,
                    overlayColor = Color(0xFFD4AF37),
                    brushStrokeWidth = 90f,
                    onRevealed = onDismiss,
                    overlayContent = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = Color(0xFF1A1A1A),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.cinema_challenge_golden_ticket_label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1A1A),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.cinema_challenge_scratch_here_start),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                        }
                    },
                    revealedContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2C1E00))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.cinema_challenge_fresh_mystery_loaded),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(id = R.string.cinema_challenge_5_new_clues_ready),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        stringResource(id = R.string.cinema_challenge_start_guessing),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun isMatchingTitleNormalized(guess: String, target: String): Boolean {
    val norm = { s: String ->
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }
    val g = norm(guess)
    val t = norm(target)
    if (g == t) return true
    val strip = { s: String -> s.removePrefix("the").removePrefix("a").removePrefix("an") }
    return strip(g) == strip(t)
}
