package dev.mmartos.advent.screen.day02

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.TopBar
import kotlinx.collections.immutable.PersistentList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Day02Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day02ViewModel = koinViewModel()
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
            modifier = Modifier.fillMaxWidth().height(320.dp),
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
            Row(
                horizontalArrangement = spacedBy(8.dp)
            ) {
                Text("Current item:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    parserStage.currentItem,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ProductIDRanges(
                productIDRanges = parserStage.productIDRanges,
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
    ProductIDRanges(
        productIDRanges = parserStage.productIDRanges,
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
private fun ProductIDRanges(
    productIDRanges: PersistentList<LongRange>,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Text("Product ID Ranges")
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        ) {
            itemsIndexed(productIDRanges) { index, range ->
                Text(
                    text = range.resolve(index),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
        LaunchedEffect(productIDRanges) {
            lazyGridState.scrollToItem(productIDRanges.size - 1)
        }
    }
}

private fun LongRange.resolve(index: Int): String =
    "Range ${String.format("%02d", index + 1)}: $this"


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
        val currentRange = (solverStage as? SolverStage1.Solving)?.currentRange
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        if (currentRange != null) {
            ProductIDRange(
                productIDRange = currentRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            solutionTextStyle = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
        val currentRange = (solverStage as? SolverStage2.Solving)?.currentRange
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        if (currentRange != null) {
            ProductIDRange(
                productIDRange = currentRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            solutionTextStyle = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
private fun ProductIDRange(
    productIDRange: LongRange,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "Range:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Row(
            horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Text(
                text = productIDRange.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
    }
}
