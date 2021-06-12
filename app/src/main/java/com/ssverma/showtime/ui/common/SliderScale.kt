package com.ssverma.showtime.ui.common

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    labelTextStyle: TextStyle = MaterialTheme.typography.caption.copy(
        color = MaterialTheme.colors.onSurface
    ),
    sliderColors: SliderColors = SliderDefaults.colors(
        inactiveTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.24f),
        inactiveTickColor = Color.Transparent
    ),
    scaleDimensions: ScaleDimensions = SliderScaleDefaults.scaleDimensions
) {

    val secondarySteps = remember(max, min, secondaryGap) { (max - min) / secondaryGap }
    val primarySteps = remember(primaryGap, secondaryGap) { primaryGap / secondaryGap }

    var sliderValue by remember { mutableStateOf(value = current) }

    Column(modifier = modifier) {
        Box(contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                for (i in 0..secondarySteps) {
                    val primaryIndicator = i % primarySteps == 0

                    if (primaryIndicator) {
                        Box(
                            primaryIndicatorModifier
                                .width(scaleDimensions.primaryIndicatorWidth)
                                .height(scaleDimensions.primaryIndicatorHeight)
                                .background(
                                    color = sliderColors.trackColor(
                                        enabled = true,
                                        active = secondaryGap * i <= sliderValue
                                    ).value,
                                    shape = SliderScaleDefaults.primaryIndicatorShape
                                )
                        )
                    } else {
                        Box(
                            secondaryIndicatorModifier
                                .width(scaleDimensions.secondaryIndicatorWidth)
                                .height(scaleDimensions.secondaryIndicatorHeight)
                                .background(
                                    color = sliderColors.trackColor(
                                        enabled = true,
                                        active = secondaryGap * i <= sliderValue
                                    ).value,
                                    shape = SliderScaleDefaults.secondaryIndicatorShape
                                )
                        )
                    }
                }
            }
            Slider(
                colors = sliderColors,
                steps = if (secondarySteps == 0) 0 else secondarySteps - 1,
                valueRange = min.toFloat()..max.toFloat(),
                value = current,
                onValueChange = {
                    sliderValue = it
                    onValueChange(it)
                }
            )
        }
        if (showLabel) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
                    .offset(y = (-8).dp)
            ) {
                for (i in 0..secondarySteps) {
                    val primaryIndicator = i % primarySteps == 0

                    if (primaryIndicator) {
                        Text(
                            text = (i * secondaryGap).toString(),
                            style = labelTextStyle
                        )
                    }
                }
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
        primaryIndicatorHeight = 20.dp,
        primaryIndicatorWidth = 2.dp,
        secondaryIndicatorHeight = 10.dp,
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