package dev.mmartos.advent.screen.day01

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.TopBar
import dev.mmartos.advent.ui.TopContainer
import kotlinx.collections.immutable.PersistentList

@Composable
fun Day01(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopContainer(
        modifier = modifier
    ) {
        TopBar(
            title = "${dayDetails.title} - Solver",
            onBackClicked = onBackClicked,
        )
    }
}