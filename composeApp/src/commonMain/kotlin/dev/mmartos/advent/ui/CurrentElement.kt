package dev.mmartos.advent.ui

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font

enum class CurrentElementLayout {
    Vertical, Horizontal
}

@Composable
fun CurrentElement(
    title: String,
    currentItem: String,
    modifier: Modifier = Modifier,
    layout: CurrentElementLayout = CurrentElementLayout.Vertical,
) {
    when (layout) {
        CurrentElementLayout.Vertical -> CurrentElementVertical(title, currentItem, modifier)
        CurrentElementLayout.Horizontal -> CurrentElementHorizontal(title, currentItem, modifier)
    }
}


@Composable
private fun CurrentElementVertical(
    title: String,
    currentItem: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = currentItem,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun CurrentElementHorizontal(
    title: String,
    currentItem: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = currentItem,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        )
    }
}