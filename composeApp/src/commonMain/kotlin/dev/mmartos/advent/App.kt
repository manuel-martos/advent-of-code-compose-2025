package dev.mmartos.advent

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import dev.mmartos.advent.AoCKey.HomeKey
import dev.mmartos.advent.AoCKey.PuzzleKey
import dev.mmartos.advent.di.appModules
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.screen.home.HomeScreen
import dev.mmartos.advent.screen.puzzleInput.PuzzleScreen
import dev.mmartos.advent.theme.AoCTheme
import dev.mmartos.advent.ui.Background
import dev.mmartos.advent.ui.modifiers.snowShader
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

sealed interface AoCKey : NavKey {
    data object HomeKey : AoCKey
    data class PuzzleKey(val dayDetails: DayDetails) : AoCKey
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
                    .snowShader()
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
                                    onDayClicked = { day -> backStack.add(PuzzleKey(day)) },
                                )
                            }

                            is PuzzleKey -> NavEntry(key = key) {
                                PuzzleScreen(
                                    dayDetails = key.dayDetails,
                                    onBackClicked = onBack,
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
