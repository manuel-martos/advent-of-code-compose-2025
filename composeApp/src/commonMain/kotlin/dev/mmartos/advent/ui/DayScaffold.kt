package dev.mmartos.advent.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.models.DayDetails
import kotlinx.collections.immutable.PersistentList

@Composable
fun <PS, SS1, SS2> DayScaffold(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    uiState: UiState<PS, SS1, SS2>,
    onStart: (PersistentList<String>) -> Unit,
    onBackClicked: () -> Unit,
    onDispose: () -> Unit,
    modifier: Modifier = Modifier,
    parsingContent: @Composable (PS, Modifier) -> Unit = @Composable { _, _ -> },
    solvingContent1: @Composable (SS1, Modifier) -> Unit = @Composable { _, _ -> },
    solvingContent2: @Composable (SS2, Modifier) -> Unit = @Composable { _, _ -> },
    parsingHeight: Dp = 360.dp,
    solvingHeight: Dp = 360.dp,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(puzzleInput) {
        onStart.invoke(puzzleInput)
    }
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(32.dp),
    ) {
        TopBar(
            title = "${dayDetails.title} - Solver",
            onBackClicked = onBackClicked,
        )
        uiState.parserStage?.run {
            parsingContent.invoke(this, Modifier.height(parsingHeight))
        }
        if (uiState.isSolving()) {
            Row(
                horizontalArrangement = spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth().height(solvingHeight),
            ) {
                var hasScrolled by remember { mutableStateOf(false) }
                uiState.solverStage1?.run {
                    solvingContent1.invoke(this, Modifier.weight(1f))
                }
                uiState.solverStage2?.run {
                    solvingContent2.invoke(this, Modifier.weight(1f))
                }
                LaunchedEffect(uiState.solverStage1, uiState.solverStage2) {
                    if (!hasScrolled && uiState.solverStage1 != null && uiState.solverStage2 != null) {
                        hasScrolled = true
                        scrollState.scrollTo(scrollState.maxValue)
                    }
                }
            }
        }
    }
    DisposableEffect(puzzleInput) {
        onDispose {
            onDispose.invoke()
        }
    }
}
