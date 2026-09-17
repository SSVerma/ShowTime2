package com.ssverma.common.ui.widget

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdded
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.common.ui.widget.component.ManualSetupGuideCard
import com.ssverma.common.ui.widget.component.UpNextWidgetPreview
import com.ssverma.common.ui.widget.component.WatchlistWidgetPreview
import com.ssverma.common.ui.widget.component.WidgetPinFallbackDialog
import com.ssverma.common.ui.widget.component.WidgetShowcaseCard
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWidgetsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    val isPinSupported = remember(context) { WidgetPinHelper.isPinningSupported(context) }
    var showFallbackDialog by remember { mutableStateOf(false) }

    val handlePinClick: (String) -> Unit = remember(context, isPinSupported) {
        { receiverClass ->
            if (isPinSupported) {
                val pinned = WidgetPinHelper.pinWidget(context, receiverClass)
                if (pinned) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.widget_pin_requested),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showFallbackDialog = true
                }
            } else {
                showFallbackDialog = true
            }
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.large)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            WidgetsHeader()

            // Widget 1: Up Next TV Widget
            WidgetShowcaseCard(
                title = stringResource(R.string.widget_up_next_card_title),
                description = stringResource(R.string.widget_up_next_card_desc),
                icon = Icons.Rounded.LiveTv,
                sizeBadge = stringResource(R.string.widget_up_next_grid_size),
                statusBadge = stringResource(R.string.widget_up_next_badge_sync),
                previewContent = { UpNextWidgetPreview() },
                onPinClicked = { handlePinClick(WidgetPinHelper.UP_NEXT_RECEIVER_CLASS) }
            )

            // Widget 2: Watchlist & Bookmarks Widget
            WidgetShowcaseCard(
                title = stringResource(R.string.widget_watchlist_card_title),
                description = stringResource(R.string.widget_watchlist_card_desc),
                icon = Icons.Rounded.BookmarkAdded,
                sizeBadge = stringResource(R.string.widget_watchlist_grid_size),
                statusBadge = stringResource(R.string.widget_watchlist_badge_bookmarks),
                previewContent = { WatchlistWidgetPreview() },
                onPinClicked = { handlePinClick(WidgetPinHelper.WATCHLIST_RECEIVER_CLASS) }
            )

            // Manual fallback / setup guide
            ManualSetupGuideCard(initiallyExpanded = !isPinSupported)
        }
    }

    if (showFallbackDialog) {
        WidgetPinFallbackDialog(
            onDismissRequest = { showFallbackDialog = false }
        )
    }
}

@Composable
private fun WidgetsHeader(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = stringResource(R.string.widgets_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Text(
            text = stringResource(R.string.widgets_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )
    }
}
