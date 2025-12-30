package dev.mmartos.advent.screen.day12

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.check_mark
import advent_of_code_compose_2025.composeapp.generated.resources.cross_mark
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.radialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
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
import dev.mmartos.advent.utils.Gradient.colorAt
import dev.mmartos.advent.utils.leadingZeros
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day12Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day12ViewModel = koinViewModel()
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
        solverContent2 = null,
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
            ShapesAndRegions(
                shapes = parserStage.partialShapes,
                regions = parserStage.partialRegions,
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
    ShapesAndRegions(
        shapes = parserStage.challenge.shapes,
        regions = parserStage.challenge.regions,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun ShapesAndRegions(
    shapes: PersistentList<Shape>,
    regions: PersistentList<Region>,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        AutoScrollingTitledList(
            items = shapes.withIndex().toPersistentList(),
            layout = AutoScrollingTitledListLayout.ListTitled,
            modifier = Modifier.weight(8f).fillMaxHeight(),
            title = { Text("Shapes") },
            itemContent = { (index, shape) ->
                Text(
                    text = "#${index + 1}: ${shape.points.joinToString { "(${it.x}, ${it.y})" }}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                )
            }
        )

        AutoScrollingTitledList(
            items = regions.withIndex().toPersistentList(),
            layout = AutoScrollingTitledListLayout.ListTitled,
            modifier = Modifier.weight(5f).fillMaxHeight(),
            title = { Text("Regions") },
            itemContent = { (index, region) ->
                Text(
                    text = "#${index.leadingZeros(4)}: " +
                            "${region.width}x${region.height} -> ${region.presents.joinToString()}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                    maxLines = 1,
                )
            }
        )
    }
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
        val region = (solverStage as? SolverStage1.Solving)?.currentRegion
            ?: (solverStage as? SolverStage1.Solved)?.lastRegion
        val layout = (solverStage as? SolverStage1.Solving)?.layout
            ?: (solverStage as? SolverStage1.Solved)?.lastLayout
            ?: persistentListOf()
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        region?.run {
            PresentsLayout(
                region = region,
                layout = layout,
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
private fun PresentsLayout(
    region: Region,
    layout: PersistentList<PlacedShape>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier
    ) {
        Canvas(Modifier.fillMaxSize().weight(1f)) {
            val regionRatio = region.height.toFloat() / region.width
            val canvasRatio = size.height / size.width
            val newRegionSize = if (regionRatio > canvasRatio) {
                Size(size.height / regionRatio, size.height)
            } else {
                Size(size.width, size.width * regionRatio)
            }
            val cellSize = Size(newRegionSize.width / region.width, newRegionSize.height / region.height)
            val center = Offset((size.width - newRegionSize.width) / 2f, (size.height - newRegionSize.height) / 2f)
            translate(center.x, center.y) {
                repeat(region.width + 1) { x ->
                    drawLine(
                        color = Color.White,
                        start = Offset(cellSize.width * x, 0f),
                        end = Offset(cellSize.width * x, newRegionSize.height),
                    )
                }
                repeat(region.height + 1) { y ->
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, cellSize.height * y),
                        end = Offset(newRegionSize.width, cellSize.height * y),
                    )
                }
                layout.forEach { placedShape ->
                    val shapePath = Path().apply {
                        placedShape.shape.points.forEachIndexed { index, point ->
                            if (index == 0) {
                                moveTo(point.x * cellSize.width, point.y * cellSize.height)
                            } else {
                                lineTo(point.x * cellSize.width, point.y * cellSize.height)
                            }
                        }
                    }
                    val offset =
                        Offset(cellSize.width * placedShape.position.x, cellSize.width * placedShape.position.y)
                    translate(offset.x, offset.y) {
                        drawPath(
                            path = shapePath,
                            brush = placedShape.id.resolveShapeBrush(3f * cellSize)
                        )
                        drawPath(
                            path = shapePath,
                            color = Color.White,
                            style = Stroke(),
                        )
                    }
                }
            }
        }
        Row(
            horizontalArrangement = spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(layout.resolveCheck()),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${region.width}x${region.height} -> ${region.presents.joinToString()}",
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}


private fun Int.resolveShapeBrush(
    shapeSize: Size,
): Brush {
    val nodeColor = colorAt((this % 16.toFloat() / 15f))
    return radialGradient(
        colors = listOf(
            nodeColor,
            nodeColor.copy(alpha = 0.65f).compositeOver(Color.Black),
        ),
        center = Offset(0.45f * shapeSize.width, 0.4f * shapeSize.height),
        radius = shapeSize.maxDimension,
    )
}

private fun PersistentList<PlacedShape>.resolveCheck(): DrawableResource =
    if (isNotEmpty()) Res.drawable.check_mark else Res.drawable.cross_mark