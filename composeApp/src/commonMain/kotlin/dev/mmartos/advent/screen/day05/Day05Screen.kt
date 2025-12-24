package dev.mmartos.advent.screen.day05

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.collections.immutable.PersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day05Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day05ViewModel = koinViewModel()
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
            CurrentElement(
                title = "Current line:",
                currentItem = parserStage.currentLine,
                layout = CurrentElementLayout.Horizontal,
            )
            Spacer(modifier = Modifier.height(8.dp))
            IngredientsDatabaseContent(
                database = parserStage.partialDatabase,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ParsedContent(
    parserStage: ParserStage.Parsed,
    modifier: Modifier = Modifier
) {
    IngredientsDatabaseContent(
        database = parserStage.database,
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
private fun IngredientsDatabaseContent(
    database: IngredientsDatabase,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier,
    ) {
        FreshIDRanges(
            freshIDRanges = database.freshIDs,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        Ingredients(
            ingredients = database.availableIngredients,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun FreshIDRanges(
    freshIDRanges: PersistentList<LongRange>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = freshIDRanges,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = {
            Text(
                text = "Fresh ID Ranges:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            )
        },
        itemContent = {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
    )
}

@Composable
private fun Ingredients(
    ingredients: PersistentList<Long>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = ingredients,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(columns = 2),
        modifier = modifier,
        title = {
            Text(
                text = "Ingredients:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            )
        },
        itemContent = {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
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
        val verifiedIngredients = (solverStage as? SolverStage1.Solving)?.verifiedIngredients
            ?: (solverStage as? SolverStage1.Solved)?.verifiedIngredients
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        verifiedIngredients?.run {
            VerifiedIngredientsContent(
                verifiedIngredients = verifiedIngredients,
                modifier = Modifier.weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Vertical,
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
        val verifiedFreshIDs = (solverStage as? SolverStage2.Solving)?.verifiedFreshIDs
            ?: (solverStage as? SolverStage2.Solved)?.verifiedFreshIDs
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        verifiedFreshIDs?.run {
            VerifiedFreshIDs(
                verifiedFreshIDs = verifiedFreshIDs,
                modifier = Modifier.weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Vertical,
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
private fun VerifiedIngredientsContent(
    verifiedIngredients: PersistentList<IngredientState>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = verifiedIngredients,
        layout = AutoScrollingTitledListLayout.GridLayoutTitled(columns = 2),
        modifier = modifier,
        title = {
            Text(
                text = "Verified Ingredients:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            )
        },
        itemContent = {
            val emoji = if (it.isFresh) "✅" else "\uD83D\uDEA8"
            val color = if (it.isFresh) Color.White else Color.Red
            val fontWeight = if (it.isFresh) FontWeight.Normal else FontWeight.Bold
            Text(
                text = "$emoji -> ${it.ingredientId}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    color = color,
                    fontWeight = fontWeight,
                ),
            )
        }
    )
}

@Composable
private fun VerifiedFreshIDs(
    verifiedFreshIDs: PersistentList<LongRange>,
    modifier: Modifier = Modifier,
) {
    AutoScrollingTitledList(
        items = verifiedFreshIDs,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = {
            Text(
                text = "Verified Fresh ID Ranges:",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                modifier = Modifier
                    .fillMaxWidth(),
            )
        },
        itemContent = {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }
    )
}