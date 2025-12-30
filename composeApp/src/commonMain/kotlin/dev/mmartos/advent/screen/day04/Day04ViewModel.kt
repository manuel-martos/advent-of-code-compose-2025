package dev.mmartos.advent.screen.day04

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.extraLongDelay
import dev.mmartos.advent.utils.Delay.longDelay
import dev.mmartos.advent.utils.Delay.tinyDelay
import dev.mmartos.advent.utils.DelayReason
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

data class PaperRollMap(
    val cols: Int,
    val rows: Int,
    val content: PersistentList<CharArray>,
) {
    fun countAdjacent(row: Int, col: Int, value: Char): Int {
        val minRow = (row - 1).coerceIn(content[0].indices)
        val minCol = (col - 1).coerceIn(content.indices)
        val maxRow = (row + 1).coerceIn(content[0].indices)
        val maxCol = (col + 1).coerceIn(content.indices)
        var result = 0
        for (curRow in minRow..maxRow) {
            for (curCol in minCol..maxCol) {
                if (curRow == row && curCol == col) {
                    continue
                }
                if (content[curRow][curCol] == value) {
                    result++
                }
            }
        }
        return result
    }

    fun updateCell(row: Int, col: Int, value: Char) {
        content[row][col] = value
    }
}


sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialPaperRollMap: PaperRollMap,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val paperRollMap: PaperRollMap,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val paperRollMap: PaperRollMap,
        val validCells: PersistentList<Pair<Int, Int>>,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val paperRollMap: PaperRollMap,
        val validCells: PersistentList<Pair<Int, Int>>,
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val paperRollMap: PaperRollMap,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val paperRollMap: PaperRollMap,
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day04UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day04ViewModel : BaseViewModel<ParserStage, PaperRollMap, SolverStage1, SolverStage2>() {

    override val Day04UiState.parsedData: PaperRollMap?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.paperRollMap

    fun startParser(input: List<String>) = doParsing {
        val content = mutableListOf<CharArray>()
        runCatching {
            input.forEach { line ->
                content += line.toCharArray()
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialPaperRollMap = PaperRollMap(
                                cols = content.first().size,
                                rows = content.size,
                                content = content.toPersistentList(),
                            ),
                        ),
                    )
                }
                longDelay(DelayReason.Parser)
            }
            if (!content.isValid()) {
                error("Invalid input")
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
                        PaperRollMap(
                            cols = content.first().size,
                            rows = content.size,
                            content = content.toPersistentList(),
                        )
                    ),
                )
            }
            solvePart1()
//            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        val validCells = mutableListOf<Pair<Int, Int>>()
        uiState.value.parsedData?.run {
            content.indices.forEach { row ->
                content[row].indices.forEach { col ->
                    val currentCell = row to col
                    if (content[row][col] == '@' && countAdjacent(row, col, '@') < 4) {
                        validCells.add(currentCell)
                    }
                    uiStateUpdater.update {
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                paperRollMap = this,
                                validCells = validCells.toPersistentList(),
                                partialSolution = validCells.size.toString(),
                            )
                        )
                    }
                }
                tinyDelay(DelayReason.Solver)
            }
            uiStateUpdater.update {
                it.copy(
                    solverStage1 = SolverStage1.Solved(
                        paperRollMap = this,
                        validCells = validCells.toPersistentList(),
                        solution = validCells.size.toString(),
                    )
                )
            }
        }
    }

    fun solvePart2() = doSolving {
        var result = 0
        val validCells = mutableListOf<Pair<Int, Int>>()
        uiState.value.parsedData?.copy(
            content = uiState.value.parsedData?.content?.map { it.copyOf() }?.toPersistentList()!!
        )?.run {
            do {
                validCells.clear()
                content.indices.forEach { row ->
                    content[row].indices.forEach { col ->
                        val currentCell = row to col
                        if (content[row][col] == '@' && countAdjacent(row, col, '@') < 4) {
                            validCells.add(currentCell)
                        }
                    }
                }
                validCells.forEach {
                    updateCell(it.first, it.second, '.')
                }
                result += validCells.size
                uiStateUpdater.update {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            paperRollMap = this,
                            partialSolution = result.toString(),
                        )
                    )
                }
                extraLongDelay(DelayReason.Solver)
            } while (validCells.isNotEmpty())
            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        paperRollMap = this,
                        solution = result.toString(),
                    )
                )
            }
        }
    }

    private fun List<CharArray>.isValid(): Boolean =
        all { it.isValid() } && with(map { it.size }.toSet()) { size == 1 }

    private fun CharArray.isValid(): Boolean =
        with(toSet()) {
            size == 2 && contains('.') && contains('@')
        }
}