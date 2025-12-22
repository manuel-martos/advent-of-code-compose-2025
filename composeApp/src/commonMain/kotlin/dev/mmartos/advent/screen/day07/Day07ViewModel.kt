package dev.mmartos.advent.screen.day07

import androidx.compose.runtime.Immutable
import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlin.math.max
import kotlin.math.roundToLong
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

@Immutable
data class TachyonDiagram(
    val cols: Int,
    val rows: Int,
    val start: Pair<Int, Int>,
    val content: PersistentList<CharArray>,
)

sealed class Timeline {
    data object Straight : Timeline()
    data object SlipLeft : Timeline()
    data object SlipRight : Timeline()
}

sealed class ParserStage {
    data class Parsing(
        val currentLine: String,
        val partialTachyonDiagram: TachyonDiagram,
    ) : ParserStage()

    data class Parsed(
        val tachyonDiagram: TachyonDiagram,
    ) : ParserStage()

    data object Error : ParserStage()
}

sealed class SolverStage1 {
    data class Solving(
        val tachyonDiagram: TachyonDiagram,
        val activeBeams: PersistentSet<Pair<Int, Int>>,
        val activeSplits: PersistentSet<Pair<Int, Int>>,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val tachyonDiagram: TachyonDiagram,
        val activeBeams: PersistentSet<Pair<Int, Int>>,
        val activeSplits: PersistentSet<Pair<Int, Int>>,
        val solution: String
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val tachyonDiagram: TachyonDiagram,
        val beams: PersistentSet<Pair<Int, Int>>,
        val beamTimeline: PersistentMap<Pair<Int, Int>, Timeline>,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val tachyonDiagram: TachyonDiagram,
        val beams: PersistentSet<Pair<Int, Int>>,
        val solution: String
    ) : SolverStage2()
}

typealias Day07UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day07ViewModel : BaseViewModel<ParserStage, TachyonDiagram, SolverStage1, SolverStage2>() {

    override val Day07UiState.parsedData: TachyonDiagram?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.tachyonDiagram

    fun startParser(input: List<String>) = doParsing {
        val content = mutableListOf<CharArray>()
        var start = -1 to -1
        runCatching {
            input.forEachIndexed { index, line ->
                if (start.first == -1 && line.contains('S')) {
                    start = index to line.indexOf('S')
                }
                content += line.toCharArray()
                _uiState.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialTachyonDiagram = TachyonDiagram(
                                cols = content.first().size,
                                rows = content.size,
                                start = start,
                                content = content.toPersistentList(),
                            ),
                        ),
                    )
                }
                delay(10)
            }
            if (!content.isValid()) {
                error("Invalid input")
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
                        tachyonDiagram = TachyonDiagram(
                            cols = content.first().size,
                            rows = content.size,
                            start = start,
                            content = content.toPersistentList(),
                        ),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        _uiState.value.parsedData?.run {
            val activeBeams = mutableSetOf<Pair<Int, Int>>()
            var currentBeams = mutableSetOf(start)
            val activeSplits = mutableSetOf<Pair<Int, Int>>()
            val range = start.first until rows - 1
            repeat(range.count()) {
                currentBeams = currentBeams.flatMap { beamPosition ->
                    val cellPosition = beamPosition.copy(first = beamPosition.first + 1)
                    val cellContent = content[cellPosition.first][cellPosition.second]
                    when (cellContent) {
                        '^' -> {
                            activeSplits.add(cellPosition)
                            result++
                            setOf(
                                cellPosition.first to cellPosition.second - 1,
                                cellPosition.first to cellPosition.second + 1,
                            )
                        }

                        else -> {
                            activeBeams.add(cellPosition)
                            setOf(cellPosition)
                        }
                    }
                }.toMutableSet()
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            tachyonDiagram = this,
                            activeBeams = activeBeams.toPersistentSet(),
                            activeSplits = activeSplits.toPersistentSet(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(5)
            }
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        tachyonDiagram = this,
                        activeBeams = activeBeams.toPersistentSet(),
                        activeSplits = activeSplits.toPersistentSet(),
                        solution = result.toString(),
                    )
                )
            }
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        _uiState.value.parsedData?.run {
            val activeBeams = mutableSetOf<Pair<Int, Int>>()
            val activeSplits = mutableSetOf<Pair<Int, Int>>()
            val dpMap = Array(rows) { LongArray(cols) { 0L } }
            dpMap[start.first][start.second] = 1L
            for (curRow in start.first until rows - 1) {

                for (curCol in 0 until cols) {
                    val ways = dpMap[curRow][curCol]
                    if (ways == 0L) continue

                    val currentCell = content[curRow + 1][curCol]
                    when (currentCell) {
                        '^' -> {
                            activeSplits.add(curRow + 1 to curCol)
                            dpMap[curRow + 1][curCol - 1] += ways
                            dpMap[curRow + 1][curCol + 1] += ways
                        }

                        else -> {
                            activeBeams.add(curRow + 1 to curCol)
                            dpMap[curRow + 1][curCol] += ways
                        }
                    }
                }
            }
            val targetCols = dpMap[rows - 1].withIndex().filter { it.value != 0L }.map { it.index }
            for (currentCol in targetCols) {
                val prevResult = result
                val maxSteps = max(dpMap[rows - 1][currentCol].coerceAtMost(cols.toLong()).toInt(), cols)
                val nextResult = prevResult + dpMap[rows - 1][currentCol]
                val stepResult = (nextResult - prevResult).toDouble() / maxSteps
                repeat(maxSteps) { curStep ->
                    var timelineRow = rows - 1
                    var timelineCol = currentCol
                    val timeline = buildMap {
                        put(timelineRow to timelineCol, Timeline.Straight)
                        while (timelineRow != start.first) {
                            val currentRow = timelineRow - 1
                            val candidates = buildMap {
                                if ((timelineCol - 1) >= 0 && content[currentRow][timelineCol - 1] == '^' && activeSplits.contains(currentRow to timelineCol - 1)) {
                                    put(currentRow to (timelineCol - 1), Timeline.SlipRight)
                                }
                                if (dpMap[currentRow][timelineCol] != 0L && (currentRow % 2 != 0)) {
                                    put(currentRow to timelineCol, Timeline.Straight)
                                }
                                if ((timelineCol + 1) < cols && content[currentRow][timelineCol + 1] == '^' && activeSplits.contains(currentRow to timelineCol + 1)) {
                                    put(currentRow to (timelineCol + 1), Timeline.SlipLeft)
                                }
                            }
                            if (candidates.isNotEmpty()) {
                                val candidate = candidates.entries.toList().shuffled().first()
                                put(candidate.key, candidate.value)
                                timelineRow = candidate.key.first
                                timelineCol = candidate.key.second
                            } else {
                                timelineRow = currentRow
                                put(timelineRow to timelineCol, Timeline.Straight)
                            }
                        }
                    }
                    _uiState.threadSafeUpdate {
                        it.copy(
                            solverStage2 = SolverStage2.Solving(
                                tachyonDiagram = this,
                                beams = activeBeams.toPersistentSet(),
                                beamTimeline = timeline.toPersistentMap(),
                                partialSolution = (prevResult + stepResult * curStep).roundToLong().toString(),
                            )
                        )
                    }
                    delay(1)
                }
                result = nextResult
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            tachyonDiagram = this,
                            beams = activeBeams.toPersistentSet(),
                            beamTimeline = persistentMapOf(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(1)
            }
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        tachyonDiagram = this,
                        beams = activeBeams.toPersistentSet(),
                        solution = result.toString(),
                    )
                )
            }
        }
    }

    private val regex = Regex("^[.\\^S]+$")

    private fun List<CharArray>.isValid(): Boolean =
        all { it.isValid() } && with(map { it.size }.toSet()) { size == 1 }

    private fun CharArray.isValid(): Boolean =
        regex.matches(this.joinToString(""))
}