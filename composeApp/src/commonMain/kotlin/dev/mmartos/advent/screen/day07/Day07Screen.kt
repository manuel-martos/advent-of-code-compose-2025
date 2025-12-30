package dev.mmartos.advent.screen.day07

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
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day07Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day07ViewModel = koinViewModel()
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
            ParsedTachyonDiagram(
                tachyonDiagram = parserStage.partialTachyonDiagram,
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
    ParsedTachyonDiagram(
        tachyonDiagram = parserStage.tachyonDiagram,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun ParsedTachyonDiagram(
    tachyonDiagram: TachyonDiagram,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = tachyonDiagram.content,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = { Text("Tachyon Diagram") },
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
        val tachyonDiagram = (solverStage as? SolverStage1.Solving)?.tachyonDiagram
            ?: (solverStage as? SolverStage1.Solved)?.tachyonDiagram
        val activeBeams = (solverStage as? SolverStage1.Solving)?.activeBeams
            ?: (solverStage as? SolverStage1.Solved)?.activeBeams
            ?: persistentSetOf()
        val activeSplits = (solverStage as? SolverStage1.Solving)?.activeSplits
            ?: (solverStage as? SolverStage1.Solved)?.activeSplits
            ?: persistentSetOf()
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        tachyonDiagram?.run {
            TachyonDiagram(
                tachyonDiagram = this,
                beams = activeBeams,
                splits = activeSplits,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}


@Composable
private fun TachyonDiagram(
    tachyonDiagram: TachyonDiagram,
    modifier: Modifier = Modifier,
    beams: PersistentSet<Pair<Int, Int>> = persistentSetOf(),
    splits: PersistentSet<Pair<Int, Int>> = persistentSetOf(),
) {
    Canvas(
        modifier = modifier
    ) {
        val minCellSize = min(size.width / tachyonDiagram.cols, size.height / tachyonDiagram.rows)
        val cellSize = Size(minCellSize, minCellSize)
        val offset = Offset(
            x = (size.width - (minCellSize * tachyonDiagram.cols)) / 2,
            y = (size.height - (minCellSize * tachyonDiagram.rows)) / 2,
        )
        val backgroundColor = Color.White.copy(alpha = 0.1f)
        drawRect(
            color = backgroundColor,
            topLeft = offset,
            size = Size(minCellSize * tachyonDiagram.cols, minCellSize * tachyonDiagram.rows),
        )
        tachyonDiagram.content.indices.forEach { row ->
            tachyonDiagram.content[row].indices.forEach { col ->
                val topLeft = offset + Offset(col * cellSize.width, row * cellSize.height)
                when {
                    tachyonDiagram.content[row][col] == 'S' -> {
                        val cellCenter = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height * 0.5f)
                        drawLine(
                            color = Color.White,
                            start = cellCenter,
                            end = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height),
                            strokeWidth = density,
                        )
                        drawCircle(
                            color = Color.White,
                            center = cellCenter,
                            radius = cellSize.width * 0.4f,
                        )
                    }

                    tachyonDiagram.content[row][col] == '^' -> {
                        val isActive = splits.contains(row to col)
                        val color = if (isActive) Color.White else Color.White.copy(alpha = 0.75f)
                        val strokeWidth = if (isActive) density else Stroke.HairlineWidth
                        drawLine(
                            color = color,
                            start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                            end = Offset(topLeft.x - cellSize.width * 0.5f, topLeft.y + cellSize.height),
                            strokeWidth = strokeWidth,
                        )
                        drawLine(
                            color = color,
                            start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                            end = Offset(topLeft.x + cellSize.width * 1.5f, topLeft.y + cellSize.height),
                            strokeWidth = strokeWidth,
                        )
                    }
                }
                if (beams.contains(row to col)) {
                    drawLine(
                        color = Color.White,
                        start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                        end = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height),
                        strokeWidth = density,
                    )
                }
            }
        }
    }
}

@Composable
private fun TachyonDiagramWithTimeline(
    tachyonDiagram: TachyonDiagram,
    modifier: Modifier = Modifier,
    beams: PersistentSet<Pair<Int, Int>> = persistentSetOf(),
    timeline: PersistentMap<Pair<Int, Int>, Timeline> = persistentMapOf(),
) {
    Canvas(
        modifier = modifier
    ) {
        val minCellSize = min(size.width / tachyonDiagram.cols, size.height / tachyonDiagram.rows)
        val cellSize = Size(minCellSize, minCellSize)
        val offset = Offset(
            x = (size.width - (minCellSize * tachyonDiagram.cols)) / 2,
            y = (size.height - (minCellSize * tachyonDiagram.rows)) / 2,
        )
        val backgroundColor = Color.White.copy(alpha = 0.1f)
        drawRect(
            color = backgroundColor,
            topLeft = offset,
            size = Size(minCellSize * tachyonDiagram.cols, minCellSize * tachyonDiagram.rows),
        )
        tachyonDiagram.content.indices.forEach { row ->
            tachyonDiagram.content[row].indices.forEach { col ->
                val topLeft = offset + Offset(col * cellSize.width, row * cellSize.height)
                when {
                    tachyonDiagram.content[row][col] == 'S' -> {
                        val cellCenter = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height * 0.5f)
                        drawLine(
                            color = Color.White,
                            start = cellCenter,
                            end = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height),
                            strokeWidth = 1.5f * density,
                        )
                        drawCircle(
                            color = Color.White,
                            center = cellCenter,
                            radius = cellSize.width * 0.4f,
                        )
                    }

                    tachyonDiagram.content[row][col] == '^' -> {
                        val isLeftBranchActive = timeline[row to col] == Timeline.SlipLeft
                        val isRightBranchActive = timeline[row to col] == Timeline.SlipRight
                        val leftBranchColor = if (isLeftBranchActive) Color.White else Color.White.copy(alpha = 0.75f)
                        val rightBranchColor = if (isRightBranchActive) Color.White else Color.White.copy(alpha = 0.75f)
                        val leftBranchStrokeWidth = if (isLeftBranchActive) 1.5f * density else Stroke.HairlineWidth
                        val rightBranchStrokeWidth = if (isRightBranchActive) 1.5f * density else Stroke.HairlineWidth
                        drawLine(
                            color = leftBranchColor,
                            start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                            end = Offset(topLeft.x - cellSize.width * 0.5f, topLeft.y + cellSize.height),
                            strokeWidth = leftBranchStrokeWidth,
                        )
                        drawLine(
                            color = rightBranchColor,
                            start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                            end = Offset(topLeft.x + cellSize.width * 1.5f, topLeft.y + cellSize.height),
                            strokeWidth = rightBranchStrokeWidth,
                        )
                    }
                }
                if (beams.contains(row to col)) {
                    val isActive = timeline[row to col] == Timeline.Straight
                    val beamColor = if (isActive) Color.White else Color.White.copy(alpha = 0.75f)
                    val beamStrokeWidth = if (isActive) 1.5f * density else Stroke.HairlineWidth
                    drawLine(
                        color = beamColor,
                        start = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y),
                        end = Offset(topLeft.x + cellSize.width * 0.5f, topLeft.y + cellSize.height),
                        strokeWidth = beamStrokeWidth,
                    )
                }
            }
        }
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
        val tachyonDiagram = (solverStage as? SolverStage2.Solving)?.tachyonDiagram
            ?: (solverStage as? SolverStage2.Solved)?.tachyonDiagram
        val beams = (solverStage as? SolverStage2.Solving)?.beams
            ?: (solverStage as? SolverStage2.Solved)?.beams
            ?: persistentSetOf()
        val timeline = (solverStage as? SolverStage2.Solving)?.beamTimeline
            ?: persistentMapOf()
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        tachyonDiagram?.run {
            TachyonDiagramWithTimeline(
                tachyonDiagram = this,
                beams = beams,
                timeline = timeline,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )

        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

