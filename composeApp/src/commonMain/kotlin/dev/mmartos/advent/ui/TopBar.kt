package dev.mmartos.advent.ui

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.arrow_back_48px
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.vectorResource

@Composable
fun TopBar(
    title: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(16.dp),
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier
                .size(56.dp)
        ) {
            Icon(
                imageVector = vectorResource(resource = Res.drawable.arrow_back_48px),
                contentDescription = null,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
    }
}