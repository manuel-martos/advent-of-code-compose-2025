package dev.mmartos.advent.screen.day01

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlin.math.abs

sealed class Direction {
    data object Left : Direction()
    data object Right : Direction()

    fun toEmoji(): String =
        when (this) {
            Left -> "↺"
            Right -> "↻"
        }
}


data class DialMovement(
    val direction: Direction,
    val steps: Int,
) {
    override fun toString(): String =
        "${direction.toEmoji()} $steps"

    fun effectiveMovement(): Int =
        steps * (if (direction == Direction.Right) 1 else -1)
}

data class DialerState(
    val currentValue: Int,
)

sealed class ParserStage {
    data class Parsing(
        val currentLine: String,
        val dialMovements: PersistentList<DialMovement>,
        val totalProgress: Double,
    ) : ParserStage()

    data class Parsed(
        val dialMovements: PersistentList<DialMovement>,
    ) : ParserStage()

    data object Error : ParserStage()
}

sealed class SolverStage1 {
    data class Solving(
        val currentDialMovement: DialMovement,
        val currentDialerState: DialerState,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val currentDialerState: DialerState,
        val solution: String
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val currentDialMovement: DialMovement,
        val currentDialerState: DialerState,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val currentDialerState: DialerState,
        val solution: String
    ) : SolverStage2()
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
                _uiState.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = currentLine,
                            dialMovements = parsedMovements.toPersistentList(),
                            totalProgress = index / maxLines.toDouble(),
                        )
                    )
                }
                delay(2)
            }
        }.onFailure {
            _uiState.update {
                it.copy(
                    parserStage = ParserStage.Error,
                )
            }
        }.onSuccess {
            _uiState.update {
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
        _uiState.value.parsedData?.forEach { movement ->
            _uiState.threadSafeUpdate {
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
            delay(1)
        }
        _uiState.threadSafeUpdate {
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
        _uiState.value.parsedData?.forEach { movement ->
            _uiState.threadSafeUpdate {
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
            delay(1)
        }
        _uiState.threadSafeUpdate {
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