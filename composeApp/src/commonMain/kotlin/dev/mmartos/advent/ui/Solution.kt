package dev.mmartos.advent.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.theme.AoCTheme

enum class SolutionLayout {
    Vertical, Horizontal
}

@Composable
fun Solution(
    solution: String,
    partial: Boolean,
    layout: SolutionLayout = SolutionLayout.Vertical,
    modifier: Modifier = Modifier,
    solutionTextStyle: TextStyle = MaterialTheme.typography.displaySmall
) {
    when (layout) {
        SolutionLayout.Vertical -> SolutionVertical(solution, partial, solutionTextStyle, modifier)
        SolutionLayout.Horizontal -> SolutionHorizontal(solution, partial, solutionTextStyle, modifier)
    }
}

@Composable
private fun SolutionVertical(
    solution: String,
    partial: Boolean,
    solutionTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        SolutionCaption(partial)
        SolutionContent(solution, solutionTextStyle)
    }
}

@Composable
private fun SolutionHorizontal(
    solution: String,
    partial: Boolean,
    solutionTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        SolutionCaption(partial)
        SolutionContent(solution, solutionTextStyle)
    }
}

@Composable
private fun SolutionCaption(
    partial: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = partial.resolve(),
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
            fontFamily = FontFamily.Monospace,
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

@Composable
@Preview
private fun SolutionVerticalPreview() {
    AoCTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Solution(
                    solution = "123412341234",
                    partial = false,
                    layout = SolutionLayout.Vertical,
                )
                Solution(
                    solution = "123412341234",
                    partial = true,
                    layout = SolutionLayout.Vertical,
                )
            }
        }
    }
}


@Composable
@Preview
private fun SolutionHorizontalPreview() {
    AoCTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Solution(
                    solution = "123412341234",
                    partial = false,
                    layout = SolutionLayout.Horizontal,
                )
                Solution(
                    solution = "123412341234",
                    partial = true,
                    layout = SolutionLayout.Horizontal,
                )
            }
        }
    }
}