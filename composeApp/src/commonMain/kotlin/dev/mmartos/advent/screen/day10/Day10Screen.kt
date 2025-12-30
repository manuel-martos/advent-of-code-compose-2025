package dev.mmartos.advent.screen.day10

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.button_normal
import advent_of_code_compose_2025.composeapp.generated.resources.button_pressed
import advent_of_code_compose_2025.composeapp.generated.resources.led_off
import advent_of_code_compose_2025.composeapp.generated.resources.led_on
import advent_of_code_compose_2025.composeapp.generated.resources.ready_off
import advent_of_code_compose_2025.composeapp.generated.resources.ready_on
import advent_of_code_compose_2025.composeapp.generated.resources.source_code_pro_regular
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Luminosity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.ui.AutoScrollingTitledList
import dev.mmartos.advent.ui.AutoScrollingTitledListLayout
import dev.mmartos.advent.ui.CurrentElement
import dev.mmartos.advent.ui.CurrentElementLayout
import dev.mmartos.advent.ui.DayScaffold
import dev.mmartos.advent.ui.ParserSection
import dev.mmartos.advent.ui.Solution
import dev.mmartos.advent.ui.SolutionLayout
import dev.mmartos.advent.ui.SolverSection
import dev.mmartos.advent.utils.leadingZeros
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Suppress("DuplicatedCode")
fun Day10Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day10ViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    DayScaffold(
        dayDetails = dayDetails,
        puzzleInput = puzzleInput,
        uiState = uiState,
        onStart = { input -> vm.startParser(input) },
        onBackClicked = onBackClicked,
        onDispose = { vm.stop() },
        parserContent = { parserStage, modifier ->
            ParserSection(
                parserStage = parserStage,
                modifier = modifier,
            )
        },
        solverContent1 = { solverStage, modifier ->
            Solver1Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        solverContent2 = { solverStage, modifier ->
            Solver2Section(
                solverStage = solverStage,
                modifier = modifier,
            )
        },
        modifier = modifier,
        parsingHeight = 360.dp,
        solvingHeight = 360.dp,
    )
}

@Composable
private fun ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
) {
    ParserSection(
        parserStage = parserStage,
        modifier = modifier,
        parsingContent = { parsingStage: ParserStage.Parsing, modifier: Modifier ->
            ParsingContent(
                parserStage = parsingStage,
                modifier = modifier
            )
        },
        parsedContent = { parsedStage: ParserStage.Parsed, modifier: Modifier ->
            ParsedContent(
                parserStage = parsedStage,
                modifier = modifier
            )
        },
    )
}

@Composable
private fun ParsingContent(
    parserStage: ParserStage.Parsing,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CurrentElement(
                title = "Current line:",
                currentItem = parserStage.currentLine,
                layout = CurrentElementLayout.Vertical,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Machines(
                machines = parserStage.partialMachines,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun ParsedContent(
    parserStage: ParserStage.Parsed,
    modifier: Modifier = Modifier
) {
    Machines(
        machines = parserStage.machines,
        modifier = modifier.fillMaxSize(),
    )
}


@Composable
private fun Machines(
    machines: PersistentList<Machine>,
    modifier: Modifier = Modifier
) {
    val maxLights = machines.maxOf { it.nLights }
    AutoScrollingTitledList(
        items = machines,
        layout = AutoScrollingTitledListLayout.ListTitled,
        modifier = modifier,
        title = { Text("Machines") },
        itemContent = { machine ->
            Row {
                Text(
                    text = "#${machine.id.leadingZeros(3)}: ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily(),
                        fontWeight = FontWeight.ExtraLight,
                    ),
                    maxLines = 1,
                )
                Text(
                    text = "[${machine.target}]".padEnd(maxLights + 5),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily(),
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
                val joltages = machine.joltage.joinToString(", ") { it.leadingZeros(3) }
                Text(
                    text = "{$joltages}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily(),
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                )
            }
        }
    )
}


@Composable
private fun Solver1Section(
    solverStage: SolverStage1,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val currentMachine = (solverStage as? SolverStage1.Solving)?.currentMachine
            ?: (solverStage as? SolverStage1.Solved)?.lastMachine
        val currentState = (solverStage as? SolverStage1.Solving)?.currentState
            ?: (solverStage as? SolverStage1.Solved)?.lastMachine?.targetMask!!
        val currentButtonIndex = (solverStage as? SolverStage1.Solving)?.currentButtonIndex
        val machineReady = (solverStage as? SolverStage1.Solving)?.machineReady
            ?: true
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        currentMachine?.run {
            MachineWithLightsDiagram(
                machine = currentMachine,
                currentState = currentState,
                currentButtonIndex = currentButtonIndex,
                machineReady = machineReady,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage1.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}


@Composable
private fun MachineWithLightsDiagram(
    machine: Machine,
    currentState: Int,
    currentButtonIndex: Int?,
    machineReady: Boolean,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp, Alignment.CenterVertically),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium),
    ) {
        Text(
            text = "Machine #${machine.id.leadingZeros(3)}",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
        Image(
            painter = machineReady.resolveReadyState(),
            contentDescription = null,
            modifier = Modifier
                .width(80.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        ) {
            (0 until machine.nLights).forEach { lightIndex ->
                Image(
                    painter = currentState.resolveLightState(lightIndex),
                    contentDescription = null,
                    modifier = Modifier
                        .width(32.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        ) {
            (0 until machine.nButtons).forEach { buttonIndex ->
                Image(
                    painter = currentButtonIndex.resolveButtonState(buttonIndex),
                    contentDescription = null,
                    colorFilter = currentButtonIndex.resolveButtonColorFilter(buttonIndex),
                    modifier = Modifier
                        .width(24.dp)
                        .clip(CircleShape),
                )
            }
        }
    }
}

@Composable
private fun Boolean.resolveReadyState(): Painter =
    if (this) painterResource(Res.drawable.ready_on) else painterResource(Res.drawable.ready_off)

@Composable
private fun Int.resolveLightState(lightIndex: Int): Painter =
    if (this and (1 shl lightIndex) != 0) painterResource(Res.drawable.led_on) else painterResource(Res.drawable.led_off)

@Composable
private fun Int?.resolveButtonState(buttonIndex: Int): Painter =
    if (this != null && this == buttonIndex) painterResource(Res.drawable.button_pressed) else painterResource(Res.drawable.button_normal)

@Composable
private fun Int?.resolveButtonColorFilter(buttonIndex: Int): ColorFilter? =
    if (this != null && this == buttonIndex) ColorFilter.tint(Color.LightGray.copy(alpha = 0.4f), Luminosity) else null

@Composable
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SolverSection(
        solverStage = solverStage,
        modifier = modifier
            .fillMaxSize(),
    ) { solverStage ->
        val currentMachine = (solverStage as? SolverStage2.Solving)?.currentMachine
            ?: (solverStage as? SolverStage2.Solved)?.lastMachine
        val currentJoltages = (solverStage as? SolverStage2.Solving)?.currentJoltages
            ?: (solverStage as? SolverStage2.Solved)?.lastMachine?.joltage!!
        val currentButtonIndex = (solverStage as? SolverStage2.Solving)?.currentButtonIndex
        val machineReady = (solverStage as? SolverStage2.Solving)?.machineReady
            ?: true
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        currentMachine?.run {
            MachineWithJoltageDiagram(
                machine = currentMachine,
                currentJoltages = currentJoltages,
                currentButtonIndex = currentButtonIndex,
                machineReady = machineReady,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        }
        Solution(
            solution = currentSolution,
            partial = solverStage is SolverStage2.Solving,
            layout = SolutionLayout.Horizontal,
            solutionTextStyle = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}


@Composable
private fun MachineWithJoltageDiagram(
    machine: Machine,
    currentJoltages: PersistentList<Int>,
    currentButtonIndex: Int?,
    machineReady: Boolean,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(8.dp, Alignment.CenterVertically),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium),
    ) {
        Text(
            text = "Machine #${machine.id.leadingZeros(3)}",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
        Image(
            painter = machineReady.resolveReadyState(),
            contentDescription = null,
            modifier = Modifier
                .width(80.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(32.dp),
            horizontalArrangement = spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            (0 until machine.nLights).forEach { lightIndex ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        .padding(2.dp)
                ) {
                    Text(
                        text = currentJoltages[lightIndex].leadingZeros(3),
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = Font(Res.font.source_code_pro_regular).toFontFamily()),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
        ) {
            (0 until machine.nButtons).forEach { buttonIndex ->
                Image(
                    painter = currentButtonIndex.resolveButtonState(buttonIndex),
                    contentDescription = null,
                    colorFilter = currentButtonIndex.resolveButtonColorFilter(buttonIndex),
                    modifier = Modifier
                        .width(24.dp)
                        .clip(CircleShape),
                )
            }
        }
    }
}

