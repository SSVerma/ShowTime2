package com.ssverma.core.ui.component

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            modifier = Modifier.padding(vertical = 16.dp)
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
                labelFormatter = labelFormatter
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
            modifier = Modifier.padding(vertical = 16.dp)
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
                labelFormatter = labelFormatter
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
    labelFormatter: (Float) -> String
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
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
        primaryIndicatorHeight = 32.dp,
        primaryIndicatorWidth = 2.dp,
        secondaryIndicatorHeight = 16.dp,
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