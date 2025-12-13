package dev.mmartos.advent.screen.puzzleInput

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.screen.day01.Day01Screen
import dev.mmartos.advent.screen.puzzleInput.PuzzleKey.PuzzleInputKey
import dev.mmartos.advent.screen.puzzleInput.PuzzleKey.SolvePuzzleKey
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.TopBar
import dev.mmartos.advent.ui.TopContainer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

sealed interface PuzzleKey : NavKey {
    data object PuzzleInputKey : PuzzleKey
    data class SolvePuzzleKey(val data: PersistentList<String>) : PuzzleKey
}

@Composable
fun PuzzleScreen(
    dayDetails: DayDetails,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopContainer(
        modifier = modifier,
    ) {
        val backStack = remember { mutableStateListOf<PuzzleKey>(PuzzleInputKey) }
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            entryProvider = { key ->
                when (key) {
                    is PuzzleInputKey -> NavEntry(key = key) {
                        PuzzleInput(
                            dayDetails = dayDetails,
                            onSolvePuzzleClicked = { backStack.add(SolvePuzzleKey(it)) },
                            onBackClicked = onBackClicked,
                        )
                    }

                    is SolvePuzzleKey -> NavEntry(key = key) {
                        Day01Screen(
                            dayDetails = dayDetails,
                            puzzleInput = key.data,
                            onBackClicked = onBackClicked,
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun PuzzleInput(
    dayDetails: DayDetails,
    onSolvePuzzleClicked: (PersistentList<String>) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var puzzleInput by remember { mutableStateOf("") }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(48.dp),
    ) {
        TopBar(
            title = "${dayDetails.title} - Puzzle Input",
            onBackClicked = onBackClicked,
        )
        Text(
            text = "Ensure that the text you enter here matches the format required for the puzzle of the selected day.",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            textAlign = TextAlign.Center,
        )
        SectionContainer(
            title = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            TextField(
                value = puzzleInput,
                onValueChange = { puzzleInput = it },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Button(
            onClick = { onSolvePuzzleClicked(puzzleInput.split('\n').toPersistentList()) },
            modifier = Modifier.requiredWidth(240.dp),
        ) {
            Text("Solve Puzzle", style = MaterialTheme.typography.titleLarge)
        }
    }
}