package dev.mmartos.advent.screen.puzzleInput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.TopBar

@Composable
fun PuzzleInputScreen(
    dayDetails: DayDetails,
    onSolvePuzzleClicked: (DayDetails, List<String>) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var puzzleInput by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(64.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(32.dp))
            .dropShadow(
                shape = RoundedCornerShape(32.dp),
                shadow = Shadow(
                    radius = 10.dp,
                    spread = 10.dp,
                    color = Color(0x40000000),
                )
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(48.dp)
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
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            TextField(
                value = puzzleInput,
                onValueChange = { puzzleInput = it },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Button(
            onClick = { onSolvePuzzleClicked(dayDetails, puzzleInput.split('\n')) },
            modifier = Modifier.requiredWidth(240.dp),
        ) {
            Text("Solve Puzzle", style = MaterialTheme.typography.titleLarge)
        }
    }
}