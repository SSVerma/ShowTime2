package com.ssverma.shared.ui.component.reminder

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.utils.formatLocally
import com.ssverma.shared.ui.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiringReminderSheet(
    mediaTitle: String,
    airDate: LocalDate,
    hasReminder: Boolean,
    onConfirm: (leadDays: Int, hour: Int, minute: Int) -> Unit,
    onRemove: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    episodeSubtitle: String? = null,
    initialLeadDays: Int = 0,
    initialHour: Int = 9,
    initialMinute: Int = 0
) {
    // Auto-select the first available option if the initial one has passed
    val now = remember { LocalDateTime.now() }
    val initialOption = remember(initialLeadDays, initialHour) {
        val requested = ReminderScheduleOption(
            titleRes = 0, leadDays = initialLeadDays, hour = initialHour, minute = initialMinute
        )
        if (requested.isAvailableFor(airDate, now)) {
            requested
        } else {
            DefaultReminderScheduleOptions.firstOrNull { it.isAvailableFor(airDate, now) }
        }
    }

    var selectedLeadDays by remember(initialOption) {
        mutableIntStateOf(initialOption?.leadDays ?: initialLeadDays)
    }
    var selectedHour by remember(initialOption) {
        mutableIntStateOf(initialOption?.hour ?: initialHour)
    }
    var selectedMinute by remember(initialOption) {
        mutableIntStateOf(initialOption?.minute ?: initialMinute)
    }

    val isSelectedTimePassed by remember(airDate, selectedLeadDays, selectedHour, selectedMinute) {
        derivedStateOf {
            val targetDate = airDate.minusDays(selectedLeadDays.toLong())
            val targetDateTime =
                LocalDateTime.of(targetDate, LocalTime.of(selectedHour, selectedMinute))
            !targetDateTime.isAfter(now)
        }
    }

    val formattedPreviewDate by remember(airDate, selectedLeadDays, selectedHour, selectedMinute) {
        derivedStateOf {
            val targetDate = airDate.minusDays(selectedLeadDays.toLong())
            val targetTime = LocalTime.of(selectedHour, selectedMinute)
            val dateStr = targetDate.formatLocally() ?: targetDate.toString()
            val timeStr = targetTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            "$dateStr at $timeStr"
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.extraLarge)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = stringResource(id = R.string.cd_reminder_bell),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.airing_reminder_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val subtitle = if (!episodeSubtitle.isNullOrBlank()) {
                        "$mediaTitle · $episodeSubtitle"
                    } else {
                        mediaTitle
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasReminder) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = stringResource(id = R.string.airing_reminder_active_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Rationale Card
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = stringResource(id = R.string.airing_reminder_rationale_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.airing_reminder_rationale_notifications),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Autorenew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.airing_reminder_rationale_rolling),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Lead Time Selector Header & Options
            Text(
                text = stringResource(id = R.string.airing_reminder_lead_time_header),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            ReminderLeadTimeOptions(
                options = DefaultReminderScheduleOptions,
                airDate = airDate,
                selectedLeadDays = selectedLeadDays,
                selectedHour = selectedHour,
                onOptionSelected = { option ->
                    selectedLeadDays = option.leadDays
                    selectedHour = option.hour
                    selectedMinute = option.minute
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Delivery Preview Pill
            val previewColor = if (isSelectedTimePassed) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val previewIconTint = if (isSelectedTimePassed) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }
            val previewTextColor = if (isSelectedTimePassed) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val previewIcon = if (isSelectedTimePassed) {
                Icons.Rounded.Warning
            } else {
                Icons.Rounded.Schedule
            }
            val previewText = if (isSelectedTimePassed) {
                stringResource(id = R.string.airing_reminder_passed_warning)
            } else {
                stringResource(
                    id = R.string.airing_reminder_delivery_preview,
                    formattedPreviewDate
                )
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = previewColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small
                    )
                ) {
                    Icon(
                        imageVector = previewIcon,
                        contentDescription = null,
                        tint = previewIconTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = previewTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Action Buttons
            Button(
                onClick = {
                    onConfirm(selectedLeadDays, selectedHour, selectedMinute)
                },
                enabled = !isSelectedTimePassed,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(
                        id = if (hasReminder) {
                            R.string.airing_reminder_update_action
                        } else {
                            R.string.airing_reminder_set_action
                        }
                    )
                )
            }

            if (hasReminder) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                OutlinedButton(
                    onClick = onRemove,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = stringResource(id = R.string.airing_reminder_remove_action),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
