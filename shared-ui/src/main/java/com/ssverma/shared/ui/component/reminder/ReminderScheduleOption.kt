package com.ssverma.shared.ui.component.reminder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReminderScheduleOption(
    val titleRes: Int,
    val leadDays: Int,
    val hour: Int,
    val minute: Int = 0
)

fun ReminderScheduleOption.isAvailableFor(
    airDate: LocalDate,
    now: LocalDateTime = LocalDateTime.now()
): Boolean {
    val targetDate = airDate.minusDays(leadDays.toLong())
    val targetDateTime = LocalDateTime.of(targetDate, LocalTime.of(hour, minute))
    return targetDateTime.isAfter(now)
}

val DefaultReminderScheduleOptions = listOf(
    ReminderScheduleOption(
        titleRes = R.string.airing_reminder_option_air_day_morning,
        leadDays = 0,
        hour = 9,
        minute = 0
    ),
    ReminderScheduleOption(
        titleRes = R.string.airing_reminder_option_air_day_evening,
        leadDays = 0,
        hour = 18,
        minute = 0
    ),
    ReminderScheduleOption(
        titleRes = R.string.airing_reminder_option_one_day_before,
        leadDays = 1,
        hour = 9,
        minute = 0
    ),
    ReminderScheduleOption(
        titleRes = R.string.airing_reminder_option_two_days_before,
        leadDays = 2,
        hour = 9,
        minute = 0
    )
)

@Composable
fun ReminderLeadTimeOptions(
    options: List<ReminderScheduleOption>,
    airDate: LocalDate,
    selectedLeadDays: Int,
    selectedHour: Int,
    onOptionSelected: (ReminderScheduleOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = remember(airDate) { LocalDateTime.now() }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowOptions.forEach { option ->
                    val isAvailable = option.isAvailableFor(airDate, now)
                    val isSelected =
                        isAvailable && option.leadDays == selectedLeadDays && option.hour == selectedHour

                    Surface(
                        onClick = { onOptionSelected(option) },
                        enabled = isAvailable,
                        shape = MaterialTheme.shapes.small,
                        color = when {
                            !isAvailable -> MaterialTheme.colorScheme.surfaceContainerLow
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = when {
                                !isAvailable -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            val baseTitle = stringResource(id = option.titleRes)
                            val displayText = if (isAvailable) {
                                baseTitle
                            } else {
                                stringResource(
                                    id = R.string.airing_reminder_option_passed,
                                    baseTitle
                                )
                            }
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    !isAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
