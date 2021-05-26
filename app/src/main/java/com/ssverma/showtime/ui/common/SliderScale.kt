package com.ssverma.showtime.ui.common

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
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
    primaryLineModifier: Modifier = Modifier,
    secondaryLineModifier: Modifier = Modifier,
    showLabel: Boolean = true,
    labelTextStyle: TextStyle = MaterialTheme.typography.caption,
    sliderColors: SliderColors = SliderDefaults.colors()
) {

    val secondarySteps = remember(max, min, secondaryGap) { (max - min) / secondaryGap }
    val primarySteps = remember(primaryGap, secondaryGap) { primaryGap / secondaryGap }

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
                            primaryLineModifier
                                .width(2.dp)
                                .height(20.dp)
                                .background(
                                    color = sliderColors.thumbColor(enabled = true).value,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    } else {
                        Box(
                            secondaryLineModifier
                                .width(2.dp)
                                .height(10.dp)
                                .background(
                                    color = sliderColors.thumbColor(enabled = false).value,
                                    shape = RoundedCornerShape(4.dp)
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
                onValueChange = onValueChange
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