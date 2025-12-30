package dev.mmartos.advent.ui

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font

enum class SolutionLayout {
    Vertical, Horizontal
}

@Composable
fun Solution(
    solution: String,
    partial: Boolean,
    modifier: Modifier = Modifier,
    layout: SolutionLayout = SolutionLayout.Vertical,
    solutionTextStyle: TextStyle = MaterialTheme.typography.displaySmall,
    customCaption: String? = null,
) {
    when (layout) {
        SolutionLayout.Vertical -> SolutionVertical(solution, customCaption, partial, solutionTextStyle, modifier)
        SolutionLayout.Horizontal -> SolutionHorizontal(solution, customCaption, partial, solutionTextStyle, modifier)
    }
}

@Composable
private fun SolutionVertical(
    solution: String,
    customCaption: String?,
    partial: Boolean,
    solutionTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        SolutionCaption(customCaption, partial)
        SolutionContent(solution, solutionTextStyle)
    }
}

@Composable
private fun SolutionHorizontal(
    solution: String,
    customCaption: String?,
    partial: Boolean,
    solutionTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        SolutionCaption(customCaption, partial)
        SolutionContent(solution, solutionTextStyle)
    }
}

@Composable
private fun SolutionCaption(
    customCaption: String?,
    partial: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = customCaption ?: partial.resolve(),
        style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        modifier = modifier,
    )
}


@Composable
private fun SolutionContent(
    solution: String,
    solutionTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = solution,
        style = solutionTextStyle.copy(
            color = Color.White,
            fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily(),
            textAlign = TextAlign.Center,
        ),
        modifier = modifier
            .widthIn(160.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
            .padding(8.dp),
    )
}

private fun Boolean.resolve(): String =
    if (this) "Current Solution:" else "Final Solution:"
