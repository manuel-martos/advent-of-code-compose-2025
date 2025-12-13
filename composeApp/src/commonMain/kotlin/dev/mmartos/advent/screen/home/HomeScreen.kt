package dev.mmartos.advent.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.Title
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDayClicked: (DayDetails) -> Unit,
) {
    val vm: HomeViewModel = koinViewModel()
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(48.dp)
    ) {
        Title(title = "Advent of Code · 2025")
        AdventCalendar(
            modifier = Modifier.fillMaxWidth(),
            onDayClicked = { day -> onDayClicked(vm.getDayDetails(day)) },
        )
    }
}

@Composable
private fun AdventCalendar(
    modifier: Modifier = Modifier,
    onDayClicked: (Int) -> Unit,
) {
    val outerCorner = 32.dp
    val innerCorner = 28.dp
    FlowRow(
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .wrapContentWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(outerCorner))
            .dropShadow(
                shape = RoundedCornerShape(outerCorner),
                shadow = Shadow(
                    radius = 10.dp,
                    spread = 10.dp,
                    color = Color(0x40000000),
                )
            )
            .padding(4.dp)
    ) {
        val cellModifier = Modifier.size(150.dp).padding(4.dp)
        val redCellModifier = cellModifier
            .background(Color(0xFFAD0202), RoundedCornerShape(innerCorner))
            .clip(RoundedCornerShape(innerCorner))
        val greyCellModifier = cellModifier
            .background(Color.Gray, RoundedCornerShape(innerCorner))
            .clip(RoundedCornerShape(innerCorner))
        (1..12).forEach { currentDay ->
            val curCellModifier = if ((currentDay % 2) == 0) greyCellModifier else redCellModifier
            Box(
                modifier = curCellModifier
                    .clickable { onDayClicked(currentDay) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$currentDay",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
@Preview
private fun MainScreenPreview() {
    HomeScreen(
        onDayClicked = {},
    )
}