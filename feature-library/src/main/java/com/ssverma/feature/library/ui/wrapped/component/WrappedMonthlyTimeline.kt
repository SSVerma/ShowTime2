package com.ssverma.feature.library.ui.wrapped.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.stats.MonthActivity

@Composable
fun WrappedMonthlyTimeline(
    monthlyDistribution: List<MonthActivity>,
    mostActiveMonth: MonthActivity?,
    modifier: Modifier = Modifier
) {
    val maxCount = (monthlyDistribution.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "📅 Monthly Rhythm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (mostActiveMonth != null && mostActiveMonth.count > 0) {
                            "Peak Month: ${mostActiveMonth.monthName} (${mostActiveMonth.count} logged)"
                        } else {
                            "Activity spread across months"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 12-Month Bar Chart
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                monthlyDistribution.forEach { month ->
                    val barHeightRatio =
                        if (maxCount > 0) month.count.toFloat() / maxCount.toFloat() else 0f
                    val animatedRatio by animateFloatAsState(
                        targetValue = barHeightRatio.coerceIn(0.05f, 1f),
                        label = "month_bar"
                    )

                    val isPeak = mostActiveMonth?.monthIndex == month.monthIndex && month.count > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .fillMaxHeight(animatedRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (isPeak) MaterialTheme.colorScheme.primary
                                        else if (month.count > 0) MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.5f
                                        )
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = month.monthName.take(1),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
