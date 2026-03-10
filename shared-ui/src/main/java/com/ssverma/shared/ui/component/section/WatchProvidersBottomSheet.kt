package com.ssverma.shared.ui.component.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchProvidersBottomSheet(
    watchProviderUiState: UiState<WatchProvider?, Any?>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.where_to_watch),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (watchProviderUiState) {
                is UiState.Loading -> {
                    MediaListItemShimmer(modifier = Modifier.padding(horizontal = 16.dp))
                }
                is UiState.Error<*> -> {
                    Text(
                        text = stringResource(id = R.string.na),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
                is UiState.Success<WatchProvider?> -> {
                    val watchProvider = watchProviderUiState.data
                    if (watchProvider == null || !watchProvider.hasProviders) {
                        Text(
                            text = stringResource(id = R.string.na),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                    } else {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            WatchProvidersSection(
                                watchProvider = watchProvider,
                                modifier = Modifier.fillMaxWidth(),
                                showTitle = false
                            )
                        }
                    }
                }
                else -> {
                    /* No op for Idle or unexpected states */
                }

            }
        }
    }
}
