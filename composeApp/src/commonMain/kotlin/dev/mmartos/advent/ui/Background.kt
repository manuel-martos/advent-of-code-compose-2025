package dev.mmartos.advent.ui

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Background(
    modifier: Modifier = Modifier,
) {
    val time by produceState(0f) {
        while (isActive) {
            withInfiniteAnimationFrameMillis {
                value = it / 1_000f
            }
        }
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray.copy(0.75f))
    ) {
        val maxDimension = (if (maxWidth.value > maxHeight.value) maxWidth.value else maxHeight.value)
        Spacer(
            modifier = Modifier
                .requiredWidth((2f * maxDimension).dp)
                .requiredHeight(maxDimension.dp)
                .zIndex(5f)
                .offset(y = ((0.65f + 0.075f * sin(0.45f * time)) * maxDimension).dp)
                .rotate(30f + 5f * cos(1.2f * time))
                .dropShadow(
                    shape = RectangleShape,
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 10.dp,
                        color = Color(0x40000000),
                    )
                )
                .background(Color(0xFFAD0202))
                .drawBehind {
                    val colWidth = size.width / 100f
                    for (curCol in 0..100) {
                        if (curCol % 2 == 0) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.8f),
                                topLeft = Offset(curCol * colWidth, 0f),
                                size = Size(colWidth, size.height)
                            )
                        }
                    }
                    for (curCol in 0..100) {
                        if (curCol % 2 == 0) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.4f),
                                topLeft = Offset(0f, curCol * colWidth),
                                size = Size(size.width, colWidth)
                            )
                        }
                    }
                }
        )
        Spacer(
            modifier = Modifier
                .requiredWidth((2f * maxDimension).dp)
                .requiredHeight(maxDimension.dp)
                .zIndex(2.5f)
                .offset(y = ((0.75f + 0.1f * sin(0.65f * time)) * maxDimension).dp)
                .rotate(-30f + 3.2f * cos(0.85f * time))
                .dropShadow(
                    shape = RectangleShape,
                    shadow = Shadow(
                        radius = 10.dp,
                        spread = 10.dp,
                        color = Color(0x40000000),
                    )
                )
                .background(Color(0xFF0202AD))
                .drawBehind {
                    val colWidth = size.width / 100f
                    for (curCol in 0..100) {
                        if (curCol % 2 == 0) {
                            drawRect(
                                color = Color.White.copy(alpha = 0.85f),
                                topLeft = Offset(curCol * colWidth, 0f),
                                size = Size(colWidth, size.height)
                            )
                        }
                    }
                }
        )
    }
}
