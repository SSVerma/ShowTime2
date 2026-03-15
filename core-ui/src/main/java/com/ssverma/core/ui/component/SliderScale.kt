package com.ssverma.core.ui.component

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SliderScale(
    @IntRange(from = 1) secondaryGap: Int,
    @IntRange(from = 1) primaryGap: Int,
    @IntRange(from = 0) min: Int,
    @IntRange(from = 0) max: Int,
    current: Float,
    onValueChange: (now: Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryIndicatorModifier: Modifier = Modifier,
    secondaryIndicatorModifier: Modifier = Modifier,
    showLabel: Boolean = true,
    labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    labelFormatter: (Float) -> String = { it.toInt().toString() },
    sliderColors: SliderColors = SliderDefaults.colors(),
    scaleDimensions: ScaleDimensions = SliderScaleDefaults.scaleDimensions
) {

    val secondarySteps = remember(max, min, secondaryGap) { (max - min) / secondaryGap }
    val primarySteps = remember(primaryGap, secondaryGap) { primaryGap / secondaryGap }

    Column(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            ScaleIndicators(
                secondarySteps = secondarySteps,
                primarySteps = primarySteps,
                sliderValue = current,
                min = min,
                max = max,
                secondaryGap = secondaryGap,
                sliderColors = sliderColors,
                scaleDimensions = scaleDimensions,
                primaryIndicatorModifier = primaryIndicatorModifier,
                secondaryIndicatorModifier = secondaryIndicatorModifier
            )
            Slider(
                colors = sliderColors,
                steps = if (secondarySteps == 0) 0 else secondarySteps - 1,
                valueRange = min.toFloat()..max.toFloat(),
                value = current,
                onValueChange = onValueChange
            )
        }
        if (showLabel) {
            ScaleLabels(
                secondarySteps = secondarySteps,
                primarySteps = primarySteps,
                secondaryGap = secondaryGap,
                labelTextStyle = labelTextStyle,
                labelFormatter = labelFormatter,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
fun RangeSliderScale(
    @IntRange(from = 1) secondaryGap: Int,
    @IntRange(from = 1) primaryGap: Int,
    @IntRange(from = 0) min: Int,
    @IntRange(from = 0) max: Int,
    currentStart: Float,
    currentEnd: Float,
    onValueChange: (start: Float, end: Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryIndicatorModifier: Modifier = Modifier,
    secondaryIndicatorModifier: Modifier = Modifier,
    showLabel: Boolean = true,
    labelTextStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    labelFormatter: (Float) -> String = { it.toInt().toString() },
    sliderColors: SliderColors = SliderDefaults.colors(),
    scaleDimensions: ScaleDimensions = SliderScaleDefaults.scaleDimensions
) {

    val secondarySteps = remember(max, min, secondaryGap) { (max - min) / secondaryGap }
    val primarySteps = remember(primaryGap, secondaryGap) { primaryGap / secondaryGap }

    Column(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            ScaleIndicators(
                secondarySteps = secondarySteps,
                primarySteps = primarySteps,
                sliderValue = currentEnd, // Use end for indicator highlights for now, or maybe range?
                sliderValueStart = currentStart,
                min = min,
                max = max,
                secondaryGap = secondaryGap,
                sliderColors = sliderColors,
                scaleDimensions = scaleDimensions,
                primaryIndicatorModifier = primaryIndicatorModifier,
                secondaryIndicatorModifier = secondaryIndicatorModifier
            )
            RangeSlider(
                colors = sliderColors,
                steps = if (secondarySteps == 0) 0 else secondarySteps - 1,
                valueRange = min.toFloat()..max.toFloat(),
                value = currentStart..currentEnd,
                onValueChange = {
                    onValueChange(it.start, it.endInclusive)
                }
            )
        }
        if (showLabel) {
            ScaleLabels(
                secondarySteps = secondarySteps,
                primarySteps = primarySteps,
                secondaryGap = secondaryGap,
                labelTextStyle = labelTextStyle,
                labelFormatter = labelFormatter,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun ScaleIndicators(
    secondarySteps: Int,
    primarySteps: Int,
    sliderValue: Float,
    min: Int,
    max: Int,
    secondaryGap: Int,
    sliderColors: SliderColors,
    scaleDimensions: ScaleDimensions,
    primaryIndicatorModifier: Modifier,
    secondaryIndicatorModifier: Modifier,
    sliderValueStart: Float? = null
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        for (i in 0..secondarySteps) {
            val primaryIndicator = i % primarySteps == 0
            val value = secondaryGap * i

            val isActive = if (sliderValueStart != null) {
                value >= sliderValueStart && value <= sliderValue
            } else {
                value <= sliderValue
            }

            val indicatorColor = if (isActive) {
                sliderColors.activeTrackColor
            } else {
                sliderColors.inactiveTrackColor
            }

            if (primaryIndicator) {
                Box(
                    primaryIndicatorModifier
                        .width(scaleDimensions.primaryIndicatorWidth)
                        .height(scaleDimensions.primaryIndicatorHeight)
                        .background(
                            color = indicatorColor,
                            shape = SliderScaleDefaults.primaryIndicatorShape
                        )
                )
            } else {
                Box(
                    secondaryIndicatorModifier
                        .width(scaleDimensions.secondaryIndicatorWidth)
                        .height(scaleDimensions.secondaryIndicatorHeight)
                        .background(
                            color = indicatorColor,
                            shape = SliderScaleDefaults.secondaryIndicatorShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ScaleLabels(
    secondarySteps: Int,
    primarySteps: Int,
    secondaryGap: Int,
    labelTextStyle: TextStyle,
    labelFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        for (i in 0..secondarySteps) {
            val primaryIndicator = i % primarySteps == 0
            if (primaryIndicator) {
                Text(
                    text = labelFormatter((i * secondaryGap).toFloat()),
                    style = labelTextStyle,
                    maxLines = 1,
                    softWrap = false
                )
            } else {
                Spacer(modifier = Modifier.size(0.dp))
            }
        }
    }
}

data class ScaleDimensions(
    val primaryIndicatorHeight: Dp,
    val primaryIndicatorWidth: Dp,
    val secondaryIndicatorHeight: Dp,
    val secondaryIndicatorWidth: Dp,
)

object SliderScaleDefaults {
    val scaleDimensions = ScaleDimensions(
        primaryIndicatorHeight = 40.dp,
        primaryIndicatorWidth = 2.dp,
        secondaryIndicatorHeight = 24.dp,
        secondaryIndicatorWidth = 1.dp
    )

    val primaryIndicatorShape = RoundedCornerShape(16.dp)
    val secondaryIndicatorShape = RoundedCornerShape(4.dp)
}

@Preview
@Composable
fun SliderScalePreview() {
    SliderScale(
        secondaryGap = 1,
        primaryGap = 5,
        min = 0,
        max = 10,
        current = 0f,
        onValueChange = {})
}