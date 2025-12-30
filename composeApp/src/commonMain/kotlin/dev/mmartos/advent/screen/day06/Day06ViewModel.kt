package dev.mmartos.advent.screen.day06

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.screen.day06.MathOperator.Companion.toMathOperator
import dev.mmartos.advent.utils.Delay.regularDelay
import dev.mmartos.advent.utils.Delay.shortDelay
import dev.mmartos.advent.utils.DelayReason
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

enum class MathOperator {
    PLUS,
    MULTIPLY;

    override fun toString(): String =
        when (this) {
            PLUS -> "+"
            MULTIPLY -> "*"
        }

    companion object {
        fun String.toMathOperator(): MathOperator =
            when (this) {
                "+" -> PLUS
                "*" -> MULTIPLY
                else -> throw IllegalArgumentException("Invalid operator: $this")
            }
    }
}

data class Problem(
    val values: List<String>,
    val operator: MathOperator,
    val cols: Int,
    val rows: Int,
)

data class SolvedProblem(
    val problem: Problem,
    val solution: Long,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val partialProblems: PersistentList<Problem>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val problems: PersistentList<Problem>,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val solvedProblems: PersistentList<SolvedProblem>,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val solvedProblems: PersistentList<SolvedProblem>,
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val solvedProblems: PersistentList<SolvedProblem>,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val solvedProblems: PersistentList<SolvedProblem>,
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day06UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day06ViewModel : BaseViewModel<ParserStage, List<Problem>, SolverStage1, SolverStage2>() {

    override val Day06UiState.parsedData: List<Problem>?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.problems

    fun startParser(input: List<String>) = doParsing {
        val problems = mutableListOf<Problem>()
        runCatching {
            val operationRegex = Regex("[+*]")
            val valuesRows = input.take(input.size - 1).map { "$it " }
            val operators = operationRegex
                .findAll(input.last())
                .map { it.value.trim() }
                .map { it.toMathOperator() }
                .toList()

            val splitColumns = listOf(-1) + valuesRows[0].indices.filter { isBlankColumn(valuesRows, it) }

            val rows = valuesRows.size
            splitColumns.withIndex().windowed(2) { (colStart, colEnd) ->
                val values = valuesRows.map { line -> line.substring(colStart.value + 1, colEnd.value) }
                val cols = values.maxOf { it.length }
                Problem(
                    values = values,
                    operator = operators[colStart.index],
                    rows = rows,
                    cols = cols,
                )
            }.forEach {
                problems.add(it)
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            partialProblems = problems.toPersistentList(),
                        ),
                    )
                }
                shortDelay(DelayReason.Parser)
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
                        problems = problems.toPersistentList(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        val solvedProblems = mutableListOf<SolvedProblem>()
        uiState.value.parsedData?.run {
            forEach { problem ->
                val curValues = problem.values.map { it.trim().toLong() }
                val solution = when (problem.operator) {
                    MathOperator.PLUS -> curValues.sumOf { it }
                    MathOperator.MULTIPLY -> curValues.reduce { acc, lng -> acc * lng }
                }
                solvedProblems.add(
                    SolvedProblem(
                        problem = problem,
                        solution = solution,
                    )
                )
                result += solution
                uiStateUpdater.update {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            solvedProblems = solvedProblems.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                regularDelay(DelayReason.Solver)
            }
        }
        uiStateUpdater.update {
            it.copy(
                solverStage1 = SolverStage1.Solved(
                    solvedProblems = solvedProblems.toPersistentList(),
                    solution = result.toString(),
                )
            )
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        val solvedProblems = mutableListOf<SolvedProblem>()
        uiState.value.parsedData?.run {
            forEach { problem ->
                val curValues = (0 until problem.cols).map { col ->
                    (0 until problem.rows).map { row -> problem.values[row][col] }.joinToString("").trim().toLong()
                }
                val solution = when (problem.operator) {
                    MathOperator.PLUS -> curValues.sumOf { it }
                    MathOperator.MULTIPLY -> curValues.reduce { acc, lng -> acc * lng }
                }
                solvedProblems.add(
                    SolvedProblem(
                        problem = problem,
                        solution = solution,
                    )
                )
                result += solution
                uiStateUpdater.update {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            solvedProblems = solvedProblems.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                regularDelay(DelayReason.Solver)
            }
        }
        uiStateUpdater.update {
            it.copy(
                solverStage2 = SolverStage2.Solved(
                    solvedProblems = solvedProblems.toPersistentList(),
                    solution = result.toString(),
                )
            )
        }
    }

    private fun isBlankColumn(input: List<String>, col: Int): Boolean {
        return input.all { row -> row[col] == ' ' }
    }

}