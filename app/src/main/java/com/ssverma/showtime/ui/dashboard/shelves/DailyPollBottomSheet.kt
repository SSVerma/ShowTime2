package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.ui.component.section.DailyPollCard
import com.ssverma.shared.ui.component.section.DailyPollCardSkeleton
import com.ssverma.shared.ui.component.section.DailyPollEmptyView
import com.ssverma.shared.ui.component.section.DailyPollErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPollBottomSheet(
    poll: DailyPoll,
    isLoading: Boolean,
    errorMessage: String?,
    onOptionClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when {
                isLoading -> {
                    DailyPollCardSkeleton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                errorMessage != null -> {
                    DailyPollErrorView(
                        errorMessage = errorMessage,
                        onRetry = onRetry,
                        onDismiss = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                !poll.isEnabled || poll.options.isEmpty() || poll.question.isBlank() -> {
                    DailyPollEmptyView(
                        onDismiss = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                else -> {
                    DailyPollCard(
                        poll = poll,
                        onOptionClick = onOptionClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}
