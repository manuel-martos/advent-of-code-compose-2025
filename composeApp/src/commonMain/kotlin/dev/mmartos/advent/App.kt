package dev.mmartos.advent

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dev.mmartos.advent.di.appModules
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.screen.day01.Day01
import dev.mmartos.advent.screen.home.HomeScreen
import dev.mmartos.advent.screen.puzzleInput.PuzzleInputScreen
import dev.mmartos.advent.theme.AoCTheme
import dev.mmartos.advent.ui.Background
import dev.mmartos.advent.AoCKey.HomeKey
import dev.mmartos.advent.AoCKey.PuzzleInputKey
import dev.mmartos.advent.AoCKey.DayKey
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

sealed interface AoCKey : NavKey {
    data object HomeKey : AoCKey
    data class PuzzleInputKey(val dayDetails: DayDetails) : AoCKey
    data class DayKey(val dayDetails: DayDetails, val puzzleInput: List<String>) : AoCKey
}

@Composable
@Preview
fun App() {
    AoCTheme {
        KoinApplication(
            application = {
                modules(appModules)
            },
        ) {
            Scaffold(
                modifier = Modifier
                    .requiredSizeIn(
                        minWidth = 1024.dp,
                        minHeight = 768.dp,
                    )
            ) { paddingValues ->
                val backStack = remember { mutableStateListOf<AoCKey>(HomeKey) }
                val onBack: () -> Unit = { backStack.removeLastOrNull() }
                Background()
                NavDisplay(
                    backStack = backStack,
                    onBack = onBack,
                    modifier = Modifier.padding(paddingValues),
                    entryProvider = { key ->
                        when (key) {
                            is HomeKey -> NavEntry(key = key) {
                                HomeScreen(
                                    onDayClicked = { day -> backStack.add(PuzzleInputKey(day)) },
                                )
                            }

                            is PuzzleInputKey -> NavEntry(key = key) {
                                PuzzleInputScreen(
                                    dayDetails = key.dayDetails,
                                    onSolvePuzzleClicked = { dayDetails, puzzleInput ->
                                        backStack.removeLastOrNull()
                                        backStack.add(DayKey(dayDetails, puzzleInput))
                                    },
                                    onBackClicked = onBack,
                                )
                            }

                            is DayKey -> NavEntry(key = key) {
                                when (key.dayDetails.day) {
                                    1 -> Day01(
                                        dayDetails = key.dayDetails,
                                        puzzleInput = key.puzzleInput.toPersistentList(),
                                        onBackClicked = onBack
                                    )
                                    else ->                                 Text(
                                        text = "Missing route: current key [$key]",
                                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        textAlign = TextAlign.Center,
                                    )

                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
