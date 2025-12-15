package dev.mmartos.advent.screen.day03

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.TopBar
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Day03Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day03ViewModel = koinViewModel()
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
            modifier = Modifier.fillMaxWidth().height(480.dp),
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
            CurrentElement(
                title = "Current line:",
                currentItem = parserStage.currentLine,
                layout = CurrentElementLayout.Vertical,
            )
            Spacer(modifier = Modifier.height(8.dp))
            BatteriesBanks(
                batteriesBanks = parserStage.batteriesBanks,
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
    BatteriesBanks(
        batteriesBanks = parserStage.batteriesBanks,
        modifier = modifier,
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
private fun BatteriesBanks(
    batteriesBanks: PersistentList<String>,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Text("Batteries Banks")
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        ) {
            items(batteriesBanks) { batteriesBank ->
                Text(
                    text = batteriesBank,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
        LaunchedEffect(batteriesBanks) {
            lazyListState.scrollToItem(batteriesBanks.size - 1)
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
        val partialSolvedBatteriesBank = (solverStage as? SolverStage1.Solving)?.partialSolvedBatteriesBank
        val solvedBatteriesBanks = (solverStage as? SolverStage1.Solving)?.solvedBatteriesBanks
            ?: (solverStage as? SolverStage1.Solved)?.solvedBatteriesBanks
            ?: persistentListOf()
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        val accumulatedBatteriesBanks = if (partialSolvedBatteriesBank != null) {
            (solvedBatteriesBanks.toList() + partialSolvedBatteriesBank).toPersistentList()
        } else {
            solvedBatteriesBanks
        }
        SolvedBatteriesBanks(
            solvedBatteriesBanks = accumulatedBatteriesBanks,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
        )
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            solutionTextStyle = MaterialTheme.typography.displayMedium,
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
        val partialSolvedBatteriesBank = (solverStage as? SolverStage2.Solving)?.partialSolvedBatteriesBank
        val solvedBatteriesBanks = (solverStage as? SolverStage2.Solving)?.solvedBatteriesBanks
            ?: (solverStage as? SolverStage2.Solved)?.solvedBatteriesBanks
            ?: persistentListOf()
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        val accumulatedBatteriesBanks = if (partialSolvedBatteriesBank != null) {
            (solvedBatteriesBanks.toList() + partialSolvedBatteriesBank).toPersistentList()
        } else {
            solvedBatteriesBanks
        }
        SolvedBatteriesBanks(
            solvedBatteriesBanks = accumulatedBatteriesBanks,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterHorizontally),
        )
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            solutionTextStyle = MaterialTheme.typography.displayMedium,
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
private fun SolvedBatteriesBanks(
    solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp, Alignment.CenterVertically),
        modifier = modifier,
    ) {
        val lazyListState = rememberLazyListState()
        Text(
            text = "Solved Batteries Bank:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        LazyColumn(
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp),
        ) {
            items(solvedBatteriesBanks) { solvedBatteriesBank ->
                Text(
                    text = solvedBatteriesBank.resolve(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 1.sp,
                        maxFontSize = 12.sp,
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
        LaunchedEffect(solvedBatteriesBanks) {
            lazyListState.scrollToItem(solvedBatteriesBanks.size - 1)
        }
    }
}

@Composable
@ReadOnlyComposable
private fun SolvedBatteriesBank.resolve(): AnnotatedString =
    buildAnnotatedString {
        val highlightStyle = SpanStyle(
            background = Color.White,
            color = Color.Black,
        )
        batteriesBank.forEachIndexed { index, ch ->
            if (selectedBatteries.contains(index)) {
                withStyle(highlightStyle) {
                    append(ch)
                }
            } else {
                append(ch)
            }
        }
    }
