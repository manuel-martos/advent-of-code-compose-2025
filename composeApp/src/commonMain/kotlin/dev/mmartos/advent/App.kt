package dev.mmartos.advent

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy.Companion.dialog
import androidx.navigation3.ui.NavDisplay
import dev.mmartos.advent.di.appModules
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.screen.main.HomeScreen
import dev.mmartos.advent.screen.puzzleInput.PuzzleInputScreen
import dev.mmartos.advent.theme.AoCTheme
import dev.mmartos.advent.ui.Background
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

data object HomeKey : NavKey
data class PuzzleInputKey(val dayDetails: DayDetails) : NavKey
data class DayKey(val dayDetails: DayDetails, val puzzleInput: List<String>) : NavKey

@Composable
@Preview
fun App() {
    AoCTheme {
        KoinApplication(
            application = {
                modules(appModules)
            },
        ) {

            Scaffold { paddingValues ->
                val backStack = remember { mutableStateListOf<NavKey>(HomeKey) }
                Background()
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.padding(paddingValues),
                    entryProvider = { key ->
                        when (key) {
                            is HomeKey -> NavEntry(
                                key = key,
                            ) {
                                HomeScreen(
                                    onDayClicked = { day -> backStack.add(PuzzleInputKey(day)) },
                                )
                            }

                            is PuzzleInputKey -> NavEntry(
                                key = key,
                                metadata = dialog(),
                            ) {
                                PuzzleInputScreen(
                                    dayDetails = key.dayDetails,
                                    onSolvePuzzleClicked = { dayDetails, puzzleInput ->
                                        backStack.removeLastOrNull()
                                        backStack.add(DayKey(dayDetails, puzzleInput))
                                    },
                                    onBackClicked = { backStack.removeLastOrNull() },
                                )
                            }

                            else -> NavEntry(
                                key = key,
                            ) {
                                Text(
                                    text = "Missing route: current key [$key]",
                                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
