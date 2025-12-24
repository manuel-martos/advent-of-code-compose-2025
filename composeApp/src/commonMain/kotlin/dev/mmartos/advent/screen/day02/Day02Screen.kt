package dev.mmartos.advent.screen.day02

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.Solution
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day02Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day02ViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    DayScaffold(
        dayDetails = dayDetails,
        puzzleInput = puzzleInput,
        uiState = uiState,
        onStart = { input -> vm.startParser(input) },
        onBackClicked = onBackClicked,
        onDispose = { vm.stop() },
        parserContent = { parserStage, modifier ->
            ParserSection(
                parserStage = parserStage,
                modifier = modifier,
            )
        },
        solverContent1 = { solverStage, modifier ->
            Solver1Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        solverContent2 = { solverStage, modifier ->
            Solver2Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        modifier = modifier,
        parsingHeight = 360.dp,
        solvingHeight = 320.dp,
    )
}

@Composable
private fun ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
) {
    ParserSection(
        parserStage = parserStage,
        modifier = modifier,
        parsingContent = { parsingStage: ParserStage.Parsing, modifier: Modifier ->
            ParsingContent(
                parserStage = parsingStage,
                modifier = modifier
            )
        },
        parsedContent = { parsedStage: ParserStage.Parsed, modifier: Modifier ->
            ParsedContent(
                parserStage = parsedStage,
                modifier = modifier
            )
        },
    )
}

@Composable
private fun ParsingContent(
    parserStage: ParserStage.Parsing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CurrentElement(
                title = "Current item:",
                currentItem = parserStage.currentItem,
                layout = CurrentElementLayout.Horizontal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductIDRanges(
                productIDRanges = parserStage.productIDRanges,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ParsedContent(
    parserStage: ParserStage.Parsed,
    modifier: Modifier = Modifier
) {
    ProductIDRanges(
        productIDRanges = parserStage.productIDRanges,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun ProductIDRanges(
    productIDRanges: PersistentList<LongRange>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = productIDRanges.withIndex().toPersistentList(),
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(columns = 2),
        modifier = modifier,
        title = { Text("Product ID Ranges:") },
        itemContent = { (index, range) ->
            Text(
                text = range.resolve(index),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            )
        }
    )
}

private fun LongRange.resolve(index: Int): String =
    "Range ${String.format("%02d", index + 1)}: $this"


@Composable
private fun Solver1Section(
    solverStage: SolverStage1,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = solverStage.resolveSectionTitle(),
        outline = solverStage.resolveSectionOutlineColor(),
        modifier = modifier
            .fillMaxSize(),
    ) {
        val currentRange = (solverStage as? SolverStage1.Solving)?.currentRange
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        if (currentRange != null) {
            ProductIDRange(
                productIDRange = currentRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            solutionTextStyle = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SolverStage1.resolveSectionTitle(): String =
    when (this) {
        is SolverStage1.Solving -> "➡\uFE0F Part 1 - Solving"
        is SolverStage1.Solved -> "✅ Part 1 - Solved"
    }

@Composable
private fun SolverStage1.resolveSectionOutlineColor(): Color =
    when (this) {
        is SolverStage1.Solving -> MaterialTheme.colorScheme.outline
        is SolverStage1.Solved -> Color(0xff98fb98)
    }

@Composable
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = solverStage.resolveSectionTitle(),
        outline = solverStage.resolveSectionOutlineColor(),
        modifier = modifier
            .fillMaxSize(),
    ) {
        val currentRange = (solverStage as? SolverStage2.Solving)?.currentRange
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        if (currentRange != null) {
            ProductIDRange(
                productIDRange = currentRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            solutionTextStyle = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SolverStage2.resolveSectionTitle(): String =
    when (this) {
        is SolverStage2.Solving -> "➡\uFE0F Part 2 - Solving"
        is SolverStage2.Solved -> "✅ Part 2 - Solved"
    }

@Composable
private fun SolverStage2.resolveSectionOutlineColor(): Color =
    when (this) {
        is SolverStage2.Solving -> MaterialTheme.colorScheme.outline
        is SolverStage2.Solved -> Color(0xff98fb98)
    }

@Composable
private fun ProductIDRange(
    productIDRange: LongRange,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "Range:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Row(
            horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Text(
                text = productIDRange.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
    }
}
