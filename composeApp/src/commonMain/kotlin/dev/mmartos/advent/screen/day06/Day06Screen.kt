package dev.mmartos.advent.screen.day06

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.SolutionLayout
import kotlinx.collections.immutable.PersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day06Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day06ViewModel = koinViewModel()
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
        solvingHeight = 360.dp,
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
            Problems(
                problems = parserStage.partialProblems,
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
    Problems(
        problems = parserStage.problems,
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
private fun Problems(
    problems: PersistentList<Problem>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = problems,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(5),
        modifier = modifier,
        title = {
            Text(
                text = "Problems:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            )
        },
        itemContent = {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .border(width = 1.dp, color = Color.White, shape = MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    it.values.forEach { row ->
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                text = row,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                ),
                            )
                        }
                    }
                    Text(
                        text = it.operator.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .padding(2.dp)
                    )
                }
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
        val solvedProblems = (solverStage as? SolverStage1.Solving)?.solvedProblems
            ?: (solverStage as? SolverStage1.Solved)?.solvedProblems
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        solvedProblems?.run {
            SolvedProblems(
                solvedProblems = this,
                modifier = Modifier.weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineMedium,
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
private fun SolvedProblems(
    solvedProblems: PersistentList<SolvedProblem>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = solvedProblems,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(2),
        modifier = modifier,
        title = {
            Text(
                text = "Solved Problems:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            )
        },
        itemContent = { (problem, solution) ->
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .border(width = 1.dp, color = Color.White, shape = MaterialTheme.shapes.small)
                    .clip(MaterialTheme.shapes.small)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    problem.values.forEach { row ->
                        Row(
                            modifier = Modifier
                        ) {
                            Text(
                                text = row,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                ),
                            )
                        }
                    }
                    Text(
                        text = problem.operator.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(2.dp)
                    )
                    Text(
                        text = solution.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(2.dp)
                    )
                }
            }
        }
    )
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
        val solvedProblems = (solverStage as? SolverStage2.Solving)?.solvedProblems
            ?: (solverStage as? SolverStage2.Solved)?.solvedProblems
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        solvedProblems?.run {
            SolvedProblems(
                solvedProblems = solvedProblems,
                modifier = Modifier.weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineMedium,
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