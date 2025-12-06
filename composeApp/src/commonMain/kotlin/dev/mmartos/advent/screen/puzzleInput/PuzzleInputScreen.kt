package dev.mmartos.advent.screen.puzzleInput

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.arrow_back_24px
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
import dev.mmartos.advent.ui.Title
import org.jetbrains.compose.resources.vectorResource

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
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.arrow_back_24px),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
            Title(title = "${dayDetails.title} - Puzzle Input", modifier = Modifier.weight(1f))
        }
        Text(
            text = "Ensure that the text you enter here matches the format required for the puzzle of the selected day.",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            textAlign = TextAlign.Center,
        )
        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
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