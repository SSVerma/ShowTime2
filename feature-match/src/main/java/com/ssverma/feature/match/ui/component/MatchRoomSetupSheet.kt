package com.ssverma.feature.match.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.feature.match.R
import com.ssverma.shared.domain.model.match.MatchDeckType
import com.ssverma.shared.domain.model.match.MatchMode
import com.ssverma.shared.domain.model.match.MatchRoomConfig

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MatchRoomSetupSheet(
    sheetState: SheetState,
    initialConfig: MatchRoomConfig,
    initialPlayer1: String,
    initialPlayer2: String,
    isProOrPassActive: Boolean,
    onStartGame: (MatchRoomConfig, String, String) -> Unit,
    onUnlockPro: () -> Unit,
    onDismissRequest: () -> Unit,
    onOpenJoinRoom: (() -> Unit)? = null
) {
    var selectedMode by remember { mutableStateOf(initialConfig.mode) }
    var selectedDeckType by remember { mutableStateOf(initialConfig.deckType) }
    var selectedGenreId by remember { mutableStateOf(initialConfig.genreId ?: 28) }
    var selectedDeckSize by remember { mutableIntStateOf(initialConfig.deckSize) }
    var player1Name by remember { mutableStateOf(initialPlayer1) }
    var player2Name by remember { mutableStateOf(initialPlayer2) }

    val context = LocalContext.current
    val defaultPlayer1 = stringResource(R.string.match_room_default_player1)
    val defaultPlayer2 = stringResource(R.string.match_room_default_player2)

    val genres = remember {
        listOf(
            28 to R.string.match_room_genre_action,
            27 to R.string.match_room_genre_horror,
            35 to R.string.match_room_genre_comedy,
            878 to R.string.match_room_genre_scifi,
            53 to R.string.match_room_genre_thriller,
            10749 to R.string.match_room_genre_romance,
            16 to R.string.match_room_genre_animation,
            18 to R.string.match_room_genre_drama
        )
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.match_room_setup_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector
            Text(
                text = stringResource(R.string.match_room_setup_mode_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Couch Mode
                Card(
                    onClick = { selectedMode = MatchMode.COUCH },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMode == MatchMode.COUCH) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedMode == MatchMode.COUCH) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Weekend,
                                contentDescription = null,
                                tint = if (selectedMode == MatchMode.COUCH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.match_room_mode_couch),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.match_room_mode_couch_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Remote Mode
                Card(
                    onClick = { selectedMode = MatchMode.REMOTE },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedMode == MatchMode.REMOTE) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedMode == MatchMode.REMOTE) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Devices,
                                contentDescription = null,
                                tint = if (selectedMode == MatchMode.REMOTE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.match_room_mode_remote),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.match_room_mode_remote_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Player Names (if Couch Mode)
            if (selectedMode == MatchMode.COUCH) {
                Text(
                    text = stringResource(R.string.match_room_setup_players_header),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = player1Name,
                        onValueChange = { player1Name = it },
                        label = { Text(stringResource(R.string.match_room_setup_player1_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = player2Name,
                        onValueChange = { player2Name = it },
                        label = { Text(stringResource(R.string.match_room_setup_player2_label)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                // Remote Mode Host Name
                Text(
                    text = stringResource(R.string.match_room_setup_your_name_header),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = player1Name,
                    onValueChange = { player1Name = it },
                    label = { Text(stringResource(R.string.match_room_setup_host_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Deck Type Selector
            Text(
                text = stringResource(R.string.match_room_setup_filter_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Trending Tonight (Free)
                DeckTypeOption(
                    icon = Icons.Rounded.LocalFireDepartment,
                    title = stringResource(R.string.match_room_deck_trending),
                    description = stringResource(R.string.match_room_deck_trending_desc),
                    isSelected = selectedDeckType == MatchDeckType.TRENDING,
                    isLocked = false,
                    onClick = { selectedDeckType = MatchDeckType.TRENDING }
                )

                // My Subscriptions Only
                DeckTypeOption(
                    icon = Icons.Rounded.Subscriptions,
                    title = stringResource(R.string.match_room_deck_subscriptions),
                    description = stringResource(R.string.match_room_deck_subscriptions_desc),
                    isSelected = selectedDeckType == MatchDeckType.MY_SUBSCRIPTIONS,
                    isLocked = !isProOrPassActive,
                    onClick = {
                        if (isProOrPassActive) {
                            selectedDeckType = MatchDeckType.MY_SUBSCRIPTIONS
                        } else {
                            onUnlockPro()
                        }
                    }
                )

                // Genre Spotlight
                DeckTypeOption(
                    icon = Icons.Rounded.TheaterComedy,
                    title = stringResource(R.string.match_room_deck_genre),
                    description = stringResource(R.string.match_room_deck_genre_desc),
                    isSelected = selectedDeckType == MatchDeckType.GENRE,
                    isLocked = !isProOrPassActive,
                    onClick = {
                        if (isProOrPassActive) {
                            selectedDeckType = MatchDeckType.GENRE
                        } else {
                            onUnlockPro()
                        }
                    }
                )
            }

            // Genre Chips (if Genre selected)
            if (selectedDeckType == MatchDeckType.GENRE && isProOrPassActive) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genres.forEach { (id, nameRes) ->
                        FilterChip(
                            selected = selectedGenreId == id,
                            onClick = { selectedGenreId = id },
                            label = { Text(stringResource(nameRes)) },
                            leadingIcon = if (selectedGenreId == id) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Deck Size Selector
            Text(
                text = stringResource(R.string.match_room_setup_deck_size_header),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(15, 25, 40).forEach { size ->
                    FilterChip(
                        selected = selectedDeckSize == size,
                        onClick = { selectedDeckSize = size },
                        label = { Text(stringResource(R.string.match_room_deck_size_format, size)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start Button
            Button(
                onClick = {
                    val config = MatchRoomConfig(
                        deckType = selectedDeckType,
                        genreId = if (selectedDeckType == MatchDeckType.GENRE) selectedGenreId else null,
                        genreName = if (selectedDeckType == MatchDeckType.GENRE) {
                            genres.firstOrNull { it.first == selectedGenreId }?.second?.let {
                                context.getString(
                                    it
                                )
                            }
                        } else null,
                        deckSize = selectedDeckSize,
                        mode = selectedMode
                    )
                    onStartGame(
                        config,
                        player1Name.ifBlank { defaultPlayer1 },
                        player2Name.ifBlank { defaultPlayer2 })
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedMode == MatchMode.REMOTE) stringResource(R.string.match_room_create_remote_button) else stringResource(
                        R.string.match_room_start_deck
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick shortcut to Join Room
            if (onOpenJoinRoom != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onOpenJoinRoom()
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QrCode,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.match_room_join_prompt))
                }
            }
        }
    }
}

@Composable
private fun DeckTypeOption(
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isLocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.match_room_pro_tag),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
