package dev.mmartos.advent.screen.day11

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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.radialGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
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
import dev.mmartos.advent.ui.SolutionLayout
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day11Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day11ViewModel = koinViewModel()
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
            GraphNodes(
                graph = parserStage.partialGraph,
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
    GraphNodes(
        graph = parserStage.graph,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun GraphNodes(
    graph: Graph,
    modifier: Modifier = Modifier
) {
    AutoScrollingTitledList(
        items = graph.entries.toPersistentList(),
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = { Text("Devices") },
        itemContent = { nodeEntry ->
            Row {
                Text(
                    text = "${nodeEntry.key} ⇨ ${nodeEntry.value.joinToString(" ")}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                )
            }
        }
    )
}


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
        val sourceNode = (solverStage as? SolverStage1.Solving)?.sourceNode
            ?: (solverStage as? SolverStage1.Solved)?.sourceNode
            ?: "Unknown"
        val targetNode = (solverStage as? SolverStage1.Solving)?.targetNode
            ?: (solverStage as? SolverStage1.Solved)?.targetNode
            ?: "Unknown"
        val currentGraph = (solverStage as? SolverStage1.Solving)?.visitedGraph
            ?: (solverStage as? SolverStage1.Solved)?.lastGraph
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        currentGraph?.run {
            VisitedGraph(
                sourceNode = sourceNode,
                targetNode = targetNode,
                middleNodes = persistentListOf(),
                graph = currentGraph,
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
private fun VisitedGraph(
    sourceNode: String,
    targetNode: String,
    middleNodes: PersistentList<String>,
    graph: Graph,
    modifier: Modifier,
) {
    Canvas(modifier = modifier) {
        val nodeCount = graph.keys.size
        val nodeSize = Size(density * 16f, density * 16f)
        val nodesWithoutTarget = graph.keys - targetNode

        // Draw all the nodes around a circle
        nodesWithoutTarget.forEachIndexed { index, currentNode ->
            val nodePosition = index.toNodePosition(nodeCount, size)
            drawNode(
                node = currentNode,
                nodeSize = nodeSize,
                nodePosition = nodePosition,
                sourceNode = sourceNode,
                targetNode = targetNode,
                middleNodes = middleNodes,
            )
        }
        val targetPosition = graph.keys.indices.last.toNodePosition(nodeCount, size)
        drawNode(
            node = targetNode,
            nodeSize = nodeSize,
            nodePosition = targetPosition,
            sourceNode = sourceNode,
            targetNode = targetNode,
            middleNodes = middleNodes,
        )

        // Draw all arrows
        nodesWithoutTarget.forEachIndexed { index, currentNode ->
            graph[currentNode]?.forEach { destinationNode ->
                val sourcePosition = index.toNodePosition(nodeCount, size)
                val targetIndex = nodesWithoutTarget.toNodeIndex(destinationNode)
                val targetPosition = targetIndex.toNodePosition(nodeCount, size)
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = sourcePosition,
                    end = targetPosition,
                )
            }
        }
    }
}

private fun Set<String>.toNodeIndex(node: String): Int {
    val result = indexOf(node)
    if (result == -1) return size
    return result
}

private fun Int.toNodePosition(
    nodeCount: Int,
    size: Size,
): Offset {
    val cols = ceil(sqrt(nodeCount.toFloat())).toInt().coerceAtLeast(1)
    val rows = ceil(nodeCount / cols.toFloat()).toInt().coerceAtLeast(1)

    val row = this / cols
    val col = this % cols

    val cellW = size.width / cols
    val cellH = size.height / rows

    return Offset(
        x = col * cellW + cellW / 2f,
        y = row * cellH + cellH / 2f,
    )
}

private fun String.resolveNodeBrush(
    sourceNode: String,
    targetNode: String,
    middleNodes: List<String>,
    nodeSize: Size,
): Brush =
    if (this == sourceNode) {
        radialGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.65f).compositeOver(Color.Black),
            ),
            center = Offset(0.45f * nodeSize.width, 0.4f * nodeSize.height),
            radius = nodeSize.maxDimension,
        )
    } else if (this == targetNode) {
        radialGradient(
            colors = listOf(
                Color.Green,
                Color.Green.copy(alpha = 0.65f).compositeOver(Color.Black),
            ),
            center = Offset(0.45f * nodeSize.width, 0.4f * nodeSize.height),
            radius = nodeSize.maxDimension,
        )
    } else if (middleNodes.contains(this)) {
        radialGradient(
            colors = listOf(
                Color.Red,
                Color.Red.copy(alpha = 0.65f).compositeOver(Color.Black),
            ),
            center = Offset(0.45f * nodeSize.width, 0.4f * nodeSize.height),
            radius = nodeSize.maxDimension,
        )
    }
    else {
        val nodeColor = NodeGradient.colorAt((hashCode() % 16.toFloat() / 15f))
        radialGradient(
            colors = listOf(
                nodeColor,
                nodeColor.copy(alpha = 0.65f).compositeOver(Color.Black),
            ),
            center = Offset(0.45f * nodeSize.width, 0.4f * nodeSize.height),
            radius = nodeSize.maxDimension,
        )
    }

private fun DrawScope.drawNode(
    node: String,
    nodeSize: Size,
    nodePosition: Offset,
    sourceNode: String,
    targetNode: String,
    middleNodes: List<String>,
) {
    translate(nodePosition.x, nodePosition.y) {
        drawRoundRect(
            brush = node.resolveNodeBrush(sourceNode, targetNode, middleNodes, nodeSize),
            topLeft = Offset(-nodeSize.width / 2f, -nodeSize.height / 2f),
            cornerRadius = CornerRadius(density * 4f),
            size = nodeSize,
        )
        val borderSize = nodeSize * 0.85f
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(-borderSize.width / 2f, -borderSize.height / 2f),
            cornerRadius = CornerRadius(density * 4f * 0.85f),
            size = borderSize,
            style = Stroke()
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
@ReadOnlyComposable
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
        val sourceNode = (solverStage as? SolverStage2.Solving)?.sourceNode
            ?: (solverStage as? SolverStage2.Solved)?.sourceNode
            ?: "Unknown"
        val targetNode = (solverStage as? SolverStage2.Solving)?.targetNode
            ?: (solverStage as? SolverStage2.Solved)?.targetNode
            ?: "Unknown"
        val middleNodes = (solverStage as? SolverStage2.Solving)?.middleNodes
            ?: (solverStage as? SolverStage2.Solved)?.middleNodes
            ?: persistentListOf()
        val currentGraph = (solverStage as? SolverStage2.Solving)?.visitedGraph
            ?: (solverStage as? SolverStage2.Solved)?.lastGraph
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        currentGraph?.run {
            VisitedGraph(
                sourceNode = sourceNode,
                targetNode = targetNode,
                middleNodes = middleNodes,
                graph = currentGraph,
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

object NodeGradient {

    private val Start = Color(0xFF38BDF8)
    private val End = Color(0xFFFBBF24)

    /**
     * @param t value in range [0f, 1f]
     */
    fun colorAt(t: Float): Color {
        return lerp(Start, End, t.coerceIn(0f, 1f))
    }
}