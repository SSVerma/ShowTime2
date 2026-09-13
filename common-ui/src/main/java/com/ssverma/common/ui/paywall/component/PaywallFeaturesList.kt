package com.ssverma.common.ui.paywall.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.common.ui.R
import com.ssverma.core.ui.theme.spacing

@Immutable
private data class PaywallFeature(
    val icon: ImageVector,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int
)

private val primaryFeatures = listOf(
    PaywallFeature(
        icon = Icons.Rounded.Block,
        titleRes = R.string.pro_feature_no_ads,
        subtitleRes = R.string.pro_feature_no_ads_desc
    ),
    PaywallFeature(
        icon = Icons.Rounded.AllInclusive,
        titleRes = R.string.pro_feature_unlimited,
        subtitleRes = R.string.pro_feature_unlimited_desc
    ),
    PaywallFeature(
        icon = Icons.Rounded.CloudSync,
        titleRes = R.string.pro_feature_sync,
        subtitleRes = R.string.pro_feature_sync_desc
    )
)

private val secondaryFeatures = listOf(
    PaywallFeature(
        icon = Icons.Rounded.DarkMode,
        titleRes = R.string.pro_feature_oled,
        subtitleRes = R.string.pro_feature_oled_desc
    ),
    PaywallFeature(
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        titleRes = R.string.pro_feature_receipts,
        subtitleRes = R.string.pro_feature_receipts_desc
    ),
    PaywallFeature(
        icon = Icons.Rounded.AutoAwesome,
        titleRes = R.string.pro_feature_analytics,
        subtitleRes = R.string.pro_feature_analytics_desc
    )
)

@Composable
fun PaywallFeaturesList(
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallMedium
                )
        ) {
            primaryFeatures.forEachIndexed { index, feature ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                    )
                }
                PaywallFeatureRow(
                    icon = feature.icon,
                    title = stringResource(feature.titleRes),
                    subtitle = stringResource(feature.subtitleRes)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    secondaryFeatures.forEach { feature ->
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                        )
                        PaywallFeatureRow(
                            icon = feature.icon,
                            title = stringResource(feature.titleRes),
                            subtitle = stringResource(feature.subtitleRes)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
            )

            Surface(
                onClick = { isExpanded = !isExpanded },
                shape = MaterialTheme.shapes.small,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.extraSmall)
                ) {
                    Text(
                        text = if (isExpanded) {
                            stringResource(R.string.show_less_benefits)
                        } else {
                            stringResource(R.string.show_all_benefits)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Icon(
                        imageVector = if (isExpanded) {
                            Icons.Rounded.KeyboardArrowUp
                        } else {
                            Icons.Rounded.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
