package com.ssverma.feature.library.ui.receipt.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptStyle

@Composable
fun CinemaReceiptStyleSelector(
    selectedStyle: ReceiptStyle,
    isProActive: Boolean,
    isPassActive: Boolean,
    onStyleSelected: (ReceiptStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        labelColor = MaterialTheme.colorScheme.onSurface,
        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.receipt_style),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            val isThermal = selectedStyle == ReceiptStyle.THERMAL
            FilterChip(
                selected = isThermal,
                onClick = { onStyleSelected(ReceiptStyle.THERMAL) },
                label = {
                    Text(
                        text = stringResource(R.string.receipt_style_thermal),
                        fontWeight = if (isThermal) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isThermal,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isGold = selectedStyle == ReceiptStyle.GOLDEN_PASS
            FilterChip(
                selected = isGold,
                onClick = { onStyleSelected(ReceiptStyle.GOLDEN_PASS) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.receipt_style_gold),
                            fontWeight = if (isGold) FontWeight.Bold else FontWeight.Medium
                        )
                        if (!isProActive && !isPassActive) {
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (isGold) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isGold,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )

            val isCyber = selectedStyle == ReceiptStyle.CYBERPUNK
            FilterChip(
                selected = isCyber,
                onClick = { onStyleSelected(ReceiptStyle.CYBERPUNK) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.receipt_style_cyberpunk),
                            fontWeight = if (isCyber) FontWeight.Bold else FontWeight.Medium
                        )
                        if (!isProActive && !isPassActive) {
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = if (isCyber) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = CircleShape,
                colors = chipColors,
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isCyber,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

