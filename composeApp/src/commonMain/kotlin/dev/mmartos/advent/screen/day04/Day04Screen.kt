package dev.mmartos.advent.screen.day04

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.SolutionLayout
import dev.mmartos.advent.ui.SolverSection
import kotlin.math.min
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day04Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day04ViewModel = koinViewModel()
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
        solvingHeight = 560.dp,
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
                title = "Current line:",
                currentItem = parserStage.currentLine,
                layout = CurrentElementLayout.Vertical,
            )
            Spacer(modifier = Modifier.height(8.dp))
            PaperRollMap(
                paperRollMap = parserStage.partialPaperRollMap,
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
    PaperRollMap(
        paperRollMap = parserStage.paperRollMap,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun PaperRollMap(
    paperRollMap: PaperRollMap,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = paperRollMap.content,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = { Text("Paper Roll Map") },
        itemContent = {
            Text(
                text = it.joinToString(""),
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                maxLines = 1,
            )
        }
    )
}

@Composable
private fun Solver1Section(
    solverStage: SolverStage1,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val paperRollMap = (solverStage as? SolverStage1.Solving)?.paperRollMap
            ?: (solverStage as? SolverStage1.Solved)?.paperRollMap
        val validCells = (solverStage as? SolverStage1.Solving)?.validCells
            ?: (solverStage as? SolverStage1.Solved)?.validCells
            ?: persistentListOf()
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        paperRollMap?.run {
            PaperRollMapContent(
                paperRollMap = this,
                highlightedCells = validCells,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}


@Composable
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val paperRollMap = (solverStage as? SolverStage2.Solving)?.paperRollMap
            ?: (solverStage as? SolverStage2.Solved)?.paperRollMap
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        paperRollMap?.run {
            PaperRollMapContent(
                paperRollMap = this,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun PaperRollMapContent(
    paperRollMap: PaperRollMap,
    modifier: Modifier = Modifier,
    highlightedCells: PersistentList<Pair<Int, Int>> = persistentListOf()
) {
    Canvas(
        modifier = modifier
    ) {
        val minCellSize = min(size.width / paperRollMap.cols, size.height / paperRollMap.rows)
        val cellSize = Size(minCellSize, minCellSize)
        val offset = Offset(
            x = (size.width - (minCellSize * paperRollMap.cols)) / 2,
            y = (size.height - (minCellSize * paperRollMap.rows)) / 2,
        )
        paperRollMap.content.indices.forEach { row ->
            paperRollMap.content[row].indices.forEach { col ->
                val color = when {
                    paperRollMap.content[row][col] == '.' -> Color.White.copy(alpha = 0.05f)
                    highlightedCells.contains(Pair(row, col)) -> Color.White
                    else -> Color.White.copy(alpha = 0.5f)
                }
                val topLeft = offset + Offset(col * cellSize.width, row * cellSize.height)
                drawRect(
                    color = color,
                    topLeft = topLeft,
                    size = cellSize,
                )
            }
        }
    }
}
