package dev.mmartos.advent.screen.day04

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.TopBar
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Day04Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day04ViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    LaunchedEffect(puzzleInput) {
        vm.startParser(puzzleInput)
    }
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(48.dp),
    ) {
        TopBar(
            title = "${dayDetails.title} - Solver",
            onBackClicked = onBackClicked,
        )
        uiState.parserStage?.run {
            ParserSection(
                parserStage = this,
                modifier = Modifier.height(360.dp),
            )
        }
        Row(
            horizontalArrangement = spacedBy(48.dp),
            modifier = Modifier.fillMaxWidth().height(560.dp),
        ) {
            var hasScrolled by remember { mutableStateOf(false) }
            uiState.solverStage1?.run {
                Solver1Section(
                    solverStage = this,
                    modifier = Modifier.weight(1f),
                )
            }
            uiState.solverStage2?.run {
                Solver2Section(
                    solverStage = this,
                    modifier = Modifier.weight(1f),
                )
            }
            LaunchedEffect(uiState.solverStage1, uiState.solverStage2) {
                if (!hasScrolled && uiState.solverStage1 != null && uiState.solverStage2 != null) {
                    hasScrolled = true
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
        }
    }
    DisposableEffect(puzzleInput) {
        onDispose {
            vm.stop()
        }
    }
}

@Composable
private fun ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = parserStage.resolveSectionTitle(),
        outline = parserStage.resolveSectionOutlineColor(),
        modifier = modifier.fillMaxSize(),
    ) {
        when (val parserStage = parserStage) {
            is ParserStage.Parsing ->
                ParsingContent(
                    parserStage = parserStage,
                    modifier = Modifier
                        .fillMaxSize()
                )

            is ParserStage.Parsed ->
                ParsedContent(
                    parserStage = parserStage,
                    modifier = Modifier
                        .fillMaxSize()
                )

            is ParserStage.Error -> Text("Parser error")
        }
    }
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
            Column(
                verticalArrangement = spacedBy(8.dp)
            ) {
                Text(
                    text = "Current line:",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    parserStage.currentLine,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 4.dp),
                )
            }
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
private fun ParserStage.resolveSectionOutlineColor(): Color =
    when (this) {
        is ParserStage.Parsing -> MaterialTheme.colorScheme.outline
        is ParserStage.Parsed -> Color(0xff98fb98)
        is ParserStage.Error -> MaterialTheme.colorScheme.error
    }

@Composable
@ReadOnlyComposable
private fun ParserStage.resolveSectionTitle(): String =
    when (this) {
        is ParserStage.Parsing -> "➡\uFE0F Parsing"
        is ParserStage.Parsed -> "✅ Parsed"
        is ParserStage.Error -> "\uD83D\uDEA8 Error"
    }

@Composable
private fun PaperRollMap(
    paperRollMap: PaperRollMap,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Text("Paper Roll Map")
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        ) {
            items(paperRollMap.content) { row ->
                Text(
                    text = row.joinToString(""),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                )
            }
        }
        LaunchedEffect(paperRollMap) {
            lazyListState.scrollToItem(paperRollMap.content.size - 1)
        }
    }
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
            currentSolution,
            partial = solverStage is SolverStage1.Solving,
            modifier = Modifier
                .fillMaxWidth()
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
            currentSolution,
            partial = solverStage is SolverStage2.Solving,
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

@Composable
private fun PaperRollMapContent(
    paperRollMap: PaperRollMap,
    modifier: Modifier = Modifier,
    highlightedCells: PersistentList<Pair<Int, Int>> = persistentListOf()
) {
    Canvas(
        modifier = modifier
    ) {
        val cellSize = Size(size.width / paperRollMap.cols, size.height / paperRollMap.rows)
        paperRollMap.content.indices.forEach { row ->
            paperRollMap.content[row].indices.forEach { col ->
                val color = when {
                    paperRollMap.content[row][col] == '.' -> Color.White.copy(alpha = 0.05f)
                    highlightedCells.contains(Pair(row, col)) -> Color.White
                    else -> Color.White.copy(alpha = 0.5f)
                }
                val topLeft = Offset(col * cellSize.width, row * cellSize.height)
                drawRect(
                    color = color,
                    topLeft = topLeft,
                    size = cellSize,
                )
            }
        }
    }
}

//@Composable
//@ReadOnlyComposable
//private fun SolvedBatteriesBank.resolve(): AnnotatedString =
//    buildAnnotatedString {
//        val highlightStyle = SpanStyle(
//            background = Color.White,
//            color = Color.Black,
//        )
//        batteriesBank.forEachIndexed { index, ch ->
//            if (selectedBatteries.contains(index)) {
//                withStyle(highlightStyle) {
//                    append(ch)
//                }
//            } else {
//                append(ch)
//            }
//        }
//    }

@Composable
private fun Solution(
    solution: String,
    partial: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = if (partial) "Current Solution:" else "Final Solution:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Text(
            text = solution,
            style = MaterialTheme.typography.displaySmall.copy(
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .widthIn(160.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
                .padding(8.dp),
        )
    }
}
