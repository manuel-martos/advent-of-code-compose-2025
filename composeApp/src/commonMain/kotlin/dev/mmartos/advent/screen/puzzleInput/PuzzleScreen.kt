package dev.mmartos.advent.screen.puzzleInput

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.mmartos.advent.screen.day02.Day02Screen
import dev.mmartos.advent.screen.day03.Day03Screen
import dev.mmartos.advent.screen.day04.Day04Screen
import dev.mmartos.advent.screen.day05.Day05Screen
import dev.mmartos.advent.screen.day06.Day06Screen
import dev.mmartos.advent.screen.day07.Day07Screen
import dev.mmartos.advent.screen.day08.Day08Screen
import dev.mmartos.advent.screen.day09.Day09Screen
import dev.mmartos.advent.screen.day10.Day10Screen
import dev.mmartos.advent.screen.day11.Day11Screen
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
                        when (dayDetails.day) {
                            1 ->
                                Day01Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            2 ->
                                Day02Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            3 ->
                                Day03Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            4 ->
                                Day04Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            5 ->
                                Day05Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            6 ->
                                Day06Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            7 ->
                                Day07Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            8 ->
                                Day08Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            9 ->
                                Day09Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            10 ->
                                Day10Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )

                            11 ->
                                Day11Screen(
                                    dayDetails = dayDetails,
                                    puzzleInput = key.data,
                                    onBackClicked = onBackClicked,
                                )
                        }
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
                    .horizontalScroll(rememberScrollState())
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