package dev.mmartos.advent.screen.day04

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

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


sealed class ParserStage {
    data class Parsing(
        val currentLine: String,
        val partialPaperRollMap: PaperRollMap,
    ) : ParserStage()

    data class Parsed(
        val paperRollMap: PaperRollMap,
    ) : ParserStage()

    data object Error : ParserStage()
}

sealed class SolverStage1 {
    data class Solving(
        val paperRollMap: PaperRollMap,
        val validCells: PersistentList<Pair<Int, Int>>,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val paperRollMap: PaperRollMap,
        val validCells: PersistentList<Pair<Int, Int>>,
        val solution: String
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val paperRollMap: PaperRollMap,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val paperRollMap: PaperRollMap,
        val solution: String
    ) : SolverStage2()
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
                _uiState.update {
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
                        PaperRollMap(
                            cols = content.first().size,
                            rows = content.size,
                            content = content.toPersistentList(),
                        )
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        val validCells = mutableListOf<Pair<Int, Int>>()
        _uiState.value.parsedData?.run {
            content.indices.forEach { row ->
                content[row].indices.forEach { col ->
                    val currentCell = row to col
                    if (content[row][col] == '@' && countAdjacent(row, col, '@') < 4) {
                        validCells.add(currentCell)
                    }
                    _uiState.threadSafeUpdate {
                        it.copy(
                            solverStage1 = SolverStage1.Solving(
                                paperRollMap = this,
                                validCells = validCells.toPersistentList(),
                                partialSolution = validCells.size.toString(),
                            )
                        )
                    }
                }
                delay(1)
            }
            _uiState.threadSafeUpdate {
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
        _uiState.value.parsedData?.copy(
            content = _uiState.value.parsedData?.content?.map { it.clone() }?.toPersistentList()!!
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
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            paperRollMap = this,
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(20)
            } while (validCells.isNotEmpty())
            _uiState.threadSafeUpdate {
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