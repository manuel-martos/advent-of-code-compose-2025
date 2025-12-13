package dev.mmartos.advent.screen.day01

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mmartos.advent.models.DayDetails
import dev.mmartos.advent.theme.AoCTheme
import dev.mmartos.advent.ui.SectionContainer
import dev.mmartos.advent.ui.TopBar
import kotlinx.collections.immutable.PersistentList
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Day01Screen(
    dayDetails: DayDetails,
    puzzleInput: PersistentList<String>,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: Day01ViewModel = koinViewModel()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    LaunchedEffect(puzzleInput) {
        vm.startParser(puzzleInput)
    }
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(48.dp),
    ) {
        TopBar(
            title = "${dayDetails.title} - Solver",
            onBackClicked = onBackClicked,
        )
        uiState.parserStage?.run {
            ParserSection(
                parserStage = this,
                modifier = Modifier.height(360.dp),
            )
        }
        Row(
            horizontalArrangement = spacedBy(48.dp),
            modifier = Modifier.fillMaxWidth().height(480.dp),
        ) {
            var hasScrolled by remember { mutableStateOf(false) }
            uiState.solverStage1?.run {
                Solver1Section(
                    solverStage = this,
                    modifier = Modifier.weight(1f),
                )
            }
            uiState.solverStage2?.run {
                Solver2Section(
                    solverStage = this,
                    modifier = Modifier.weight(1f),
                )
            }
            LaunchedEffect(uiState.solverStage1, uiState.solverStage2) {
                if (!hasScrolled && uiState.solverStage1 != null && uiState.solverStage2 != null) {
                    hasScrolled = true
                    scrollState.scrollTo(scrollState.maxValue)
                }
            }
        }
    }
    DisposableEffect(puzzleInput) {
        onDispose {
            vm.stop()
        }
    }
}

@Composable
private fun ParserSection(
    parserStage: ParserStage,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = parserStage.resolveSectionTitle(),
        outline = parserStage.resolveSectionOutlineColor(),
        modifier = modifier.fillMaxSize(),
    ) {
        when (val parserStage = parserStage) {
            is ParserStage.Parsing ->
                ParsingContent(
                    parserStage = parserStage,
                    modifier = Modifier
                        .fillMaxSize()
                )

            is ParserStage.Parsed ->
                ParsedContent(
                    parserStage = parserStage,
                    modifier = Modifier
                        .fillMaxSize()
                )

            is ParserStage.Error -> Text("Parser error")
        }
    }
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
            Row(
                horizontalArrangement = spacedBy(8.dp)
            ) {
                Text("Current line:", style = MaterialTheme.typography.bodyLarge)
                Text(
                    parserStage.currentLine,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            DialMovements(
                dialMovements = parserStage.dialMovements,
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
    DialMovements(
        dialMovements = parserStage.dialMovements,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun ParserStage.resolveSectionOutlineColor(): Color =
    when (this) {
        is ParserStage.Parsing -> MaterialTheme.colorScheme.outline
        is ParserStage.Parsed -> Color(0xff98fb98)
        is ParserStage.Error -> MaterialTheme.colorScheme.error
    }

@Composable
@ReadOnlyComposable
private fun ParserStage.resolveSectionTitle(): String =
    when (this) {
        is ParserStage.Parsing -> "➡\uFE0F Parsing"
        is ParserStage.Parsed -> "✅ Parsed"
        is ParserStage.Error -> "\uD83D\uDEA8 Error"
    }

@Composable
private fun DialMovements(
    dialMovements: PersistentList<DialMovement>,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()
    Column(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp)
    ) {
        Text("Dial Movements")
        LazyVerticalGrid(
            columns = GridCells.Fixed(9),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 4.dp),
        ) {
            items(dialMovements) {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }
        LaunchedEffect(dialMovements) {
            lazyGridState.scrollToItem(dialMovements.size - 1)
        }
    }
}

@Composable
private fun Solver1Section(
    solverStage: SolverStage1,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = solverStage.resolveSectionTitle(),
        outline = solverStage.resolveSectionOutlineColor(),
        modifier = modifier
            .fillMaxSize(),
    ) {
        val markerPosition = remember { Animatable(50f) }
        val currentDialMovement = (solverStage as? SolverStage1.Solving)?.currentDialMovement
        val currentDialerState = (solverStage as? SolverStage1.Solving)?.currentDialerState
            ?: (solverStage as? SolverStage1.Solved)?.currentDialerState
            ?: DialerState(currentValue = 50)
        val currentSolution = (solverStage as? SolverStage1.Solving)?.partialSolution
            ?: (solverStage as? SolverStage1.Solved)?.solution
            ?: "Unknown"
        val targetValue = if (currentDialMovement != null) {
            (currentDialerState.currentValue + currentDialMovement.effectiveMovement())
        } else {
            currentDialerState.currentValue
        }
        if (currentDialMovement != null) {
            DialMovement(
                currentDialMovement,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        LaunchedEffect(solverStage) {
            markerPosition.snapTo(
                targetValue = targetValue.toFloat(),
            )
        }
        Dialer(
            markerPosition = markerPosition.value,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
                .align(Alignment.CenterHorizontally),
        )
        Solution(
            currentSolution,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SolverStage1.resolveSectionTitle(): String =
    when (this) {
        is SolverStage1.Solving -> "➡\uFE0F Part 1 - Solving"
        is SolverStage1.Solved -> "✅ Part 1 - Solved"
    }

@Composable
private fun SolverStage1.resolveSectionOutlineColor(): Color =
    when (this) {
        is SolverStage1.Solving -> MaterialTheme.colorScheme.outline
        is SolverStage1.Solved -> Color(0xff98fb98)
    }

@Composable
private fun Solver2Section(
    solverStage: SolverStage2,
    modifier: Modifier = Modifier,
) {
    SectionContainer(
        title = solverStage.resolveSectionTitle(),
        outline = solverStage.resolveSectionOutlineColor(),
        modifier = modifier
            .fillMaxSize(),
    ) {
        val markerPosition = remember { Animatable(50f) }
        val currentDialMovement = (solverStage as? SolverStage2.Solving)?.currentDialMovement
        val currentDialerState = (solverStage as? SolverStage2.Solving)?.currentDialerState
            ?: (solverStage as? SolverStage2.Solved)?.currentDialerState
            ?: DialerState(currentValue = 50)
        val currentSolution = (solverStage as? SolverStage2.Solving)?.partialSolution
            ?: (solverStage as? SolverStage2.Solved)?.solution
            ?: "Unknown"
        val targetValue = if (currentDialMovement != null) {
            (currentDialerState.currentValue + currentDialMovement.effectiveMovement())
        } else {
            currentDialerState.currentValue
        }
        if (currentDialMovement != null) {
            DialMovement(
                currentDialMovement,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
        LaunchedEffect(solverStage) {
            markerPosition.snapTo(
                targetValue = targetValue.toFloat(),
            )
        }
        Dialer(
            markerPosition = markerPosition.value,
            modifier = Modifier
                .weight(1f)
                .aspectRatio(ratio = 1f, matchHeightConstraintsFirst = true)
                .align(Alignment.CenterHorizontally),
        )
        Solution(
            currentSolution,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
@ReadOnlyComposable
private fun SolverStage2.resolveSectionTitle(): String =
    when (this) {
        is SolverStage2.Solving -> "➡\uFE0F Part 2 - Solving"
        is SolverStage2.Solved -> "✅ Part 2 - Solved"
    }

@Composable
private fun SolverStage2.resolveSectionOutlineColor(): Color =
    when (this) {
        is SolverStage2.Solving -> MaterialTheme.colorScheme.outline
        is SolverStage2.Solved -> Color(0xff98fb98)
    }

@Composable
private fun Dialer(
    markerPosition: Float,
    modifier: Modifier = Modifier,
) {
    val tetMeasurer = rememberTextMeasurer()
    val ringBrush = remember {
        Brush.linearGradient(
            0f to Color.White,
            1f to Color.Gray,
            end = Offset.Infinite.copy(x = 0f)
        )
    }
    val markerBrush = remember {
        Brush.radialGradient(
            0.5f to Color.White,
            1f to Color.Gray,
        )
    }
    val dialerBrush = remember {
        Brush.radialGradient(
            0f to Color.White,
            0.7f to Color.LightGray,
            1f to Color.Gray,
        )
    }
    Canvas(
        modifier
    ) {
        // Dial Ring
        val outerRingWidth = 0.1f * (size.minDimension / 2.0f)
        val middleRingWidth = 0.025f * (size.minDimension / 2.0f)
        val innerRingWidth = 0.05f * (size.minDimension / 2.0f)
        val outerRingRadius = size.minDimension / 2.0f
        val middleRingRadius = outerRingRadius - outerRingWidth
        val innerRingRadius = middleRingRadius - middleRingWidth
        drawCircle(
            brush = ringBrush,
            radius = outerRingRadius - outerRingWidth / 2f,
            style = Stroke(outerRingWidth)
        )
        drawCircle(
            color = Color.LightGray,
            radius = middleRingRadius - middleRingWidth / 2f,
            style = Stroke(middleRingWidth)
        )
        scale(scaleX = 1f, scaleY = -1f) {
            drawCircle(
                brush = ringBrush,
                radius = innerRingRadius - innerRingWidth / 2f,
                style = Stroke(innerRingWidth)
            )
        }

        // Marker
        val markerRadius = 0.02f * (size.minDimension / 2.0f)
        translate(
            top = -innerRingRadius + innerRingWidth / 2f,
        ) {
            scale(scaleX = markerRadius / (size.minDimension / 2f), scaleY = markerRadius / (size.minDimension / 2f)) {
                drawCircle(
                    brush = markerBrush,
                )
            }
        }

        // Dialer Gap
        val dialerGapWidth = 0.0125f * (size.minDimension / 2.0f)
        val dialerGapRadius = innerRingRadius - innerRingWidth
        drawCircle(
            color = Color.Black,
            radius = dialerGapRadius - dialerGapWidth / 2f,
            style = Stroke(dialerGapWidth)
        )

        // Dialer
        val dialerRadius = dialerGapRadius - dialerGapWidth
        drawCircle(
            brush = dialerBrush,
            radius = dialerRadius,
        )

        // Dialer Markers
        rotate(markerPosition / 100f * 360f) {
            val dialerMarkerRadius = dialerRadius - 0.025f * (size.minDimension / 2.0f)
            repeat(100) {
                val drawTextMarker = it % 5 == 0
                val zeroMarker = it == 0
                val curMarkerLength =
                    if (drawTextMarker) 0.1f * (size.minDimension / 2) else 0.025f * (size.minDimension / 2)
                translate(
                    size.center.x, size.center.y,
                ) {
                    rotate(
                        degrees = -it * 360 / 100f,
                        pivot = Offset.Zero
                    ) {
                        translate(
                            0f, -dialerMarkerRadius,
                        ) {
                            drawLine(
                                color = Color.Gray,
                                start = Offset.Zero,
                                end = Offset(0f, curMarkerLength),
                                strokeWidth = 3f * density,
                            )
                            drawLine(
                                color = Color.DarkGray,
                                start = Offset.Zero,
                                end = Offset(0f, curMarkerLength),
                                strokeWidth = 1f * density,
                            )

                            if (drawTextMarker) {
                                val textStyle =
                                    if (zeroMarker) TextStyle.Default.copy(fontWeight = FontWeight.Bold) else TextStyle.Default
                                val textLayoutResult = tetMeasurer.measure(
                                    text = "$it",
                                    style = textStyle
                                )
                                textLayoutResult.size
                                drawText(
                                    textLayoutResult,
                                    topLeft = Offset(
                                        -textLayoutResult.size.width / 2f,
                                        curMarkerLength + dialerGapWidth
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialMovement(
    dialMovement: DialMovement,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "Current Movement:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Row(
            horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .width(160.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Text(
                text = dialMovement.direction.toEmoji(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
            Text(
                text = dialMovement.steps.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
    }
}


@Composable
private fun Solution(
    solution: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = "Current Solution:",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Text(
            text = solution,
            style = MaterialTheme.typography.titleLarge.copy(
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .width(160.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        )
    }
}

@Preview
@Composable
fun DialerPreview() {
    AoCTheme {
        Dialer(
            markerPosition = 50f,
            modifier = Modifier.fillMaxSize().background(Color.Black),
        )
    }
}


@Preview
@Composable
fun DialMovementPreview() {
    AoCTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            verticalArrangement = spacedBy(16.dp),
        ) {
            DialMovement(
                dialMovement = DialMovement(Direction.Left, 88),
                modifier = Modifier.fillMaxWidth().background(Color.Black),
            )
            DialMovement(
                dialMovement = DialMovement(Direction.Right, 77),
                modifier = Modifier.fillMaxWidth().background(Color.Black),
            )
        }
    }
}
