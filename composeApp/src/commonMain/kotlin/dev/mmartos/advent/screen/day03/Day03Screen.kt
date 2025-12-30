package dev.mmartos.advent.screen.day03

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.SolverSection
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.Font
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day03Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day03ViewModel = koinViewModel()
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
        solvingHeight = 480.dp,
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
private fun BatteriesBanks(
    batteriesBanks: PersistentList<String>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = batteriesBanks,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = { Text("Batteries Banks") },
        itemContent = {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
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
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
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
private fun SolvedBatteriesBanks(
    solvedBatteriesBanks: PersistentList<SolvedBatteriesBank>,
    modifier: Modifier = Modifier
) {
    AutoScrollingTitledList(
        items = solvedBatteriesBanks,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = {
            Text(
                text = "Solved Batteries Bank:",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
            )
        },
        itemContent = {
            Text(
                text = it.resolve(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily(),
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
        },
    )
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
