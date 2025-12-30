package dev.mmartos.advent.ui

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.check_mark
import advent_of_code_compose_2025.composeapp.generated.resources.cross_mark
import advent_of_code_compose_2025.composeapp.generated.resources.right_arrow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.mmartos.advent.common.ParserStage
import dev.mmartos.advent.common.ParsingStage

@Composable
@Suppress("UNCHECKED_CAST")
fun <PS1 : ParsingStage, PS2 : ParserStage> ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
    parsingContent: @Composable (PS1, Modifier) -> Unit,
    parsedContent: @Composable (PS2, Modifier) -> Unit,
) {
    SectionContainer(
        title = parserStage.resolveSectionTitle(),
        outline = parserStage.resolveSectionOutlineColor(),
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            parserStage.isParsing() ->
                parsingContent.invoke(parserStage as PS1, Modifier.fillMaxSize())

            parserStage.isParsed() ->
                parsedContent.invoke(parserStage as PS2, Modifier.fillMaxSize())

            else -> Text("Parser error")
        }
    }
}

@Composable
@ReadOnlyComposable
private fun ParserStage.resolveSectionTitle(): Title =
    when {
        isParsing() -> Title(
            icon = Res.drawable.right_arrow,
            text = "Parsing"
        )

        isParsed() -> Title(
            icon = Res.drawable.check_mark,
            text = "Parsed"
        )

        else -> Title(
            icon = Res.drawable.cross_mark,
            text = "Error"
        )
    }

@Composable
private fun ParserStage.resolveSectionOutlineColor(): Color =
    when {
        isParsing() -> MaterialTheme.colorScheme.outline
        isParsed() -> Color(0xff98fb98)
        else -> MaterialTheme.colorScheme.error
    }
