package com.ssverma.feature.library.ui.receipt.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptSource

@Composable
fun CinemaReceiptEmptyState(
    selectedSource: ReceiptSource,
    selectedCustomListTitle: String?,
    modifier: Modifier = Modifier
) {
    val titleRes = when {
        selectedCustomListTitle != null -> R.string.receipt_empty_title_custom_list
        selectedSource == ReceiptSource.HISTORY -> R.string.receipt_empty_title_history
        selectedSource == ReceiptSource.THIS_MONTH -> R.string.receipt_empty_title_this_month
        selectedSource == ReceiptSource.THIS_YEAR -> R.string.receipt_empty_title_this_year
        selectedSource == ReceiptSource.LAST_90_DAYS -> R.string.receipt_empty_title_last_90_days
        selectedSource == ReceiptSource.FAVORITES -> R.string.receipt_empty_title_favorites
        selectedSource == ReceiptSource.WATCHLIST -> R.string.receipt_empty_title_watchlist
        else -> R.string.receipt_empty_title_history
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.large
                )
        ) {
            CinemaReceiptIllustration()

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Text(
                text = stringResource(id = R.string.receipt_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
            )
        }
    }
}
