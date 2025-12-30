package dev.mmartos.advent.screen.day09

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
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
import dev.mmartos.advent.utils.Point2D
import dev.mmartos.advent.utils.leadingSpaces
import kotlin.math.min
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day09Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day09ViewModel = koinViewModel()
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
            Locations(
                locations = parserStage.partialLocations,
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
    Locations(
        locations = parserStage.locations.points,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun Locations(
    locations: PersistentList<Point2D>,
    modifier: Modifier = Modifier
) {
    AutoScrollingTitledList(
        items = locations,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(5),
        modifier = modifier,
        title = { Text("Locations") },
        itemContent = {
            Text(
                text = "[${it.x.leadingSpaces(5)}, ${it.y.leadingSpaces(5)}]",
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
        val locations = (solverStage as? SolverStage1.Solving)?.locations
            ?: (solverStage as? SolverStage1.Solved)?.locations
        val currentRect = (solverStage as? SolverStage1.Solving)?.currentRect
            ?: (solverStage as? SolverStage1.Solved)?.largestRect
        val currentSolution = (solverStage as? SolverStage1.Solving)?.currentRect?.area?.toString()
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        currentRect?.run {
            RedTilesRectangle(
                locations = locations!!,
                rect = currentRect,
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
private fun RedTilesRectangle(
    locations: Locations,
    rect: RedTilesRect,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color.Black, shape = MaterialTheme.shapes.small)
                .clip(MaterialTheme.shapes.small)
                .padding(8.dp),
        ) {
            val drawWidth = (locations.max.x - locations.min.x).toFloat()
            val drawHeight = (locations.max.y - locations.min.y).toFloat()
            translate(size.width / 2, size.height / 2) {
                val scale = min(size.width / drawWidth, size.height / drawHeight)
                scale(scale, scale, Offset.Zero) {
                    translate(-locations.middle.x.toFloat(), -locations.middle.y.toFloat()) {
                        locations.points.windowed(2, 1).forEach { (start, end) ->
                            drawLine(
                                color = Color.White,
                                start = Offset(start.x.toFloat(), start.y.toFloat()),
                                end = Offset(end.x.toFloat(), end.y.toFloat()),
                            )
                        }
                        val topLeft = Offset(rect.start.x.toFloat(), rect.start.y.toFloat())
                        val rightBottom = Offset(rect.end.x.toFloat(), rect.end.y.toFloat())
                        val rectSize = Size(rightBottom.x - topLeft.x, rightBottom.y - topLeft.y)
                        drawRect(
                            color = Color.Red.copy(alpha = 0.65f),
                            topLeft = topLeft,
                            size = rectSize,
                        )
                    }
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
        val locations = (solverStage as? SolverStage2.Solving)?.locations
            ?: (solverStage as? SolverStage2.Solved)?.locations
        val currentRect = (solverStage as? SolverStage2.Solving)?.currentRect
            ?: (solverStage as? SolverStage2.Solved)?.largestRect
        val currentSolution = (solverStage as? SolverStage2.Solving)?.currentRect?.area?.toString()
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        currentRect?.run {
            RedTilesRectangle(
                locations = locations!!,
                rect = currentRect,
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

