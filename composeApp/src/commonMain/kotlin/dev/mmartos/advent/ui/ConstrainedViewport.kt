package dev.mmartos.advent.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.DpSize
import kotlin.math.min

@Composable
fun ConstrainedViewport(
    viewportSize: DpSize,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(
        contentAlignment = contentAlignment,
        modifier = modifier,
    ) {
        val scale = min(maxWidth / viewportSize.width, maxHeight / viewportSize.height)
        content.invoke(
            Modifier
                .requiredSize(viewportSize)
                .scale(scale)
        )
    }
}