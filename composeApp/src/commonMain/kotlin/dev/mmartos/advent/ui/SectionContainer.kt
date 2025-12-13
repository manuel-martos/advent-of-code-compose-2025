package dev.mmartos.advent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SectionContainer(
    title: String?,
    modifier: Modifier = Modifier,
    outline: Color = MaterialTheme.colorScheme.outline,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, outline, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f))
            .padding(16.dp),
        verticalArrangement = spacedBy(16.dp),
    ) {
        title?.run {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        content.invoke(this)
    }
}