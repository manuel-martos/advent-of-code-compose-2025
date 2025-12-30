package dev.mmartos.advent.screen.day01

import advent_of_code_compose_2025.composeapp.generated.resources.Res
import advent_of_code_compose_2025.composeapp.generated.resources.rotate_left
import advent_of_code_compose_2025.composeapp.generated.resources.rotate_right
import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.tinyDelay
import dev.mmartos.advent.utils.DelayReason
import kotlin.math.abs
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.DrawableResource
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage


sealed class Direction {
    data object Left : Direction()
    data object Right : Direction()

    fun toDrawable(): DrawableResource =
        when (this) {
            Left -> Res.drawable.rotate_left
            Right -> Res.drawable.rotate_right
        }
}

data class DialMovement(
    val direction: Direction,
    val steps: Int,
) {
    fun effectiveMovement(): Int =
        steps * (if (direction == Direction.Right) 1 else -1)
}

data class DialerState(
    val currentValue: Int,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val dialMovements: PersistentList<DialMovement>,
        val totalProgress: Double,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val dialMovements: PersistentList<DialMovement>,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val currentDialMovement: DialMovement,
        val currentDialerState: DialerState,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val currentDialerState: DialerState,
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val currentDialMovement: DialMovement,
        val currentDialerState: DialerState,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val currentDialerState: DialerState,
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day01UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day01ViewModel : BaseViewModel<ParserStage, List<DialMovement>, SolverStage1, SolverStage2>() {

    override val Day01UiState.parsedData: List<DialMovement>?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.dialMovements

    fun startParser(input: List<String>) = doParsing {
        val maxLines = input.size
        val parsedMovements = mutableListOf<DialMovement>()
        runCatching {
            input.forEachIndexed { index, currentLine ->
                val dialMovement = DialMovement(
                    direction = if (currentLine[0] == 'R') Direction.Right else Direction.Left,
                    steps = currentLine.substring(1).toInt(),
                )
                parsedMovements.add(dialMovement)
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = currentLine,
                            dialMovements = parsedMovements.toPersistentList(),
                            totalProgress = index / maxLines.toDouble(),
                        )
                    )
                }
                tinyDelay(DelayReason.Parser)
            }
        }.onFailure {
            uiStateUpdater.update {
                it.copy(
                    parserStage = ParserStage.Error,
                )
            }
        }.onSuccess {
            uiStateUpdater.update {
                it.copy(
                    parserStage = ParserStage.Parsed(
                        dialMovements = parsedMovements.toPersistentList(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var zeroCounter = 0
        var newDialerState = DialerState(50)
        var lastDialerState: DialerState = newDialerState
        uiState.value.parsedData?.forEach { movement ->
            uiStateUpdater.update {
                newDialerState = DialerState(
                    currentValue = lastDialerState.currentValue + movement.effectiveMovement(),
                )
                if (wrap(newDialerState.currentValue) == 0) {
                    zeroCounter++
                }
                it.copy(
                    solverStage1 = SolverStage1.Solving(
                        currentDialMovement = movement,
                        currentDialerState = lastDialerState,
                        partialSolution = zeroCounter.toString(),
                    )
                )
            }
            lastDialerState = newDialerState
            tinyDelay(DelayReason.Solver)
        }
        uiStateUpdater.update {
            it.copy(
                solverStage1 = SolverStage1.Solved(
                    currentDialerState = lastDialerState,
                    solution = zeroCounter.toString(),
                )
            )
        }
    }

    fun solvePart2() = doSolving {
        var zeroCounter = 0
        var newDialerState = DialerState(50)
        var lastDialerState: DialerState = newDialerState
        uiState.value.parsedData?.forEach { movement ->
            uiStateUpdater.update {
                newDialerState = DialerState(
                    currentValue = lastDialerState.currentValue + movement.effectiveMovement(),
                )
                zeroCounter += countHitsOnZero(wrap(lastDialerState.currentValue), movement.effectiveMovement())
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        currentDialMovement = movement,
                        currentDialerState = lastDialerState,
                        partialSolution = zeroCounter.toString(),
                    )
                )
            }
            lastDialerState = newDialerState
            tinyDelay(DelayReason.Solver)
        }
        uiStateUpdater.update {
            it.copy(
                solverStage2 = SolverStage2.Solved(
                    currentDialerState = lastDialerState,
                    solution = zeroCounter.toString(),
                )
            )
        }
    }

    private fun wrap(x: Int): Int =
        ((x % 100) + 100) % 100

    private fun countHitsOnZero(start: Int, delta: Int): Int {
        val distance = abs(delta).toLong()
        if (distance == 0L) return 0

        val sign = if (delta > 0) 1 else -1
        val firstRaw = if (sign == 1) {
            (100 - start) % 100
        } else {
            start % 100
        }

        var first = firstRaw.toLong()
        if (first == 0L) first = 100L

        return if (first <= distance) {
            (1L + (distance - first) / 100L).toInt()
        } else {
            0
        }
    }
}