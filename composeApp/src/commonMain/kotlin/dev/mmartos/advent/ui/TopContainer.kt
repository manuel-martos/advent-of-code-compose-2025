package dev.mmartos.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp

@Composable
fun TopContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(64.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(32.dp))
            .dropShadow(
                shape = RoundedCornerShape(32.dp),
                shadow = Shadow(
                    radius = 10.dp,
                    spread = 10.dp,
                    color = Color(0x40000000),
                )
            )
            .padding(32.dp),
        content = content,
    )
}