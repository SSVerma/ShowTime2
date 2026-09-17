package com.ssverma.feature.filter.ui.discovery.component.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.filter.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryCertificationHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CertificationSection(
    watchRegion: String,
    mediaType: MediaType,
    selectedCertification: String?,
    onCertificationSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val certifications = DiscoveryCertificationHelper.getCertifications(watchRegion, mediaType)

    Column(modifier = modifier.fillMaxWidth()) {
        FilterSectionHeader(
            title = stringResource(R.string.filter_section_certification),
            icon = Icons.Rounded.VerifiedUser,
            trailingContent = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = watchRegion.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            val isAllSelected = selectedCertification == null
            Surface(
                onClick = { onCertificationSelected(null) },
                shape = RoundedCornerShape(16.dp),
                color = if (isAllSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAllSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = 0.4f
                    )
                )
            ) {
                Text(
                    text = stringResource(R.string.all_ratings),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAllSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.smallMedium,
                        vertical = MaterialTheme.spacing.small
                    )
                )
            }

            certifications.forEach { cert ->
                val isSelected = cert == selectedCertification
                Surface(
                    onClick = { onCertificationSelected(cert) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.4f
                        )
                    )
                ) {
                    Text(
                        text = cert,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.smallMedium,
                            vertical = MaterialTheme.spacing.small
                        )
                    )
                }
            }
        }
    }
}
