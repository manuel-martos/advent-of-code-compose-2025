package dev.mmartos.advent.screen.day05

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.extraLongDelay
import dev.mmartos.advent.utils.Delay.regularDelay
import dev.mmartos.advent.utils.Delay.shortDelay
import dev.mmartos.advent.utils.DelayReason
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

data class IngredientsDatabase(
    val freshIDs: PersistentList<LongRange>,
    val availableIngredients: PersistentList<Long>,
)

data class IngredientState(
    val ingredientId: Long,
    val isFresh: Boolean,
)

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentLine: String,
        val partialDatabase: IngredientsDatabase,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val database: IngredientsDatabase,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val verifiedIngredients: PersistentList<IngredientState>,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val verifiedIngredients: PersistentList<IngredientState>,
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val verifiedFreshIDs: PersistentList<LongRange>,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val verifiedFreshIDs: PersistentList<LongRange>,
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day05UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day05ViewModel : BaseViewModel<ParserStage, IngredientsDatabase, SolverStage1, SolverStage2>() {

    override val Day05UiState.parsedData: IngredientsDatabase?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.database

    fun startParser(input: List<String>) = doParsing {
        var parsingFreshIDs = true
        val freshIDs = mutableListOf<LongRange>()
        val availableIngredients = mutableListOf<Long>()
        runCatching {
            input.forEach { line ->
                if (!line.isEmpty()) {
                    if (parsingFreshIDs) {
                        val (first, last) = line.split('-')
                        freshIDs.add(first.toLong()..last.toLong())
                    } else {
                        availableIngredients.add(line.toLong())
                    }
                } else {
                    parsingFreshIDs = false
                }
                uiStateUpdater.update {
                    it.copy(
                        parserStage = ParserStage.Parsing(
                            currentLine = line,
                            partialDatabase = IngredientsDatabase(
                                freshIDs = freshIDs.toPersistentList(),
                                availableIngredients = availableIngredients.toPersistentList(),
                            )
                        ),
                    )
                }
                regularDelay(DelayReason.Parser)
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
                        database = IngredientsDatabase(
                            freshIDs = freshIDs.toPersistentList(),
                            availableIngredients = availableIngredients.toPersistentList(),
                        )
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        val verifiedIngredients = mutableListOf<IngredientState>()
        uiState.value.parsedData?.run {
            availableIngredients.forEach { ingredient ->
                val isFresh = freshIDs.any { it.contains(ingredient) }
                verifiedIngredients.add(
                    IngredientState(
                        ingredientId = ingredient,
                        isFresh = isFresh,
                    )
                )
                if (isFresh) {
                    result++
                }
                uiStateUpdater.update {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            verifiedIngredients = verifiedIngredients.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                shortDelay(DelayReason.Solver)
            }
        }
        uiStateUpdater.update {
            it.copy(
                solverStage1 = SolverStage1.Solved(
                    verifiedIngredients = verifiedIngredients.toPersistentList(),
                    solution = result.toString(),
                )
            )
        }
    }

    fun solvePart2() = doSolving {
        val verifiedFreshIDs = mutableListOf<LongRange>()
        uiState.value.parsedData?.run {
            val sortedRanges = freshIDs.sortedBy { it.first }
            val mergedRanges = mutableListOf<LongRange>()
            var currentMerge = sortedRanges.first()
            verifiedFreshIDs.add(currentMerge)
            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                        partialSolution = mergedRanges.solution(),
                    )
                )
            }
            regularDelay(DelayReason.Solver)

            for (i in 1 until sortedRanges.size) {
                val nextRange = sortedRanges[i]
                verifiedFreshIDs.add(nextRange)
                if (nextRange.first <= currentMerge.last + 1) {
                    currentMerge = currentMerge.first..maxOf(currentMerge.last, nextRange.last)
                } else {
                    mergedRanges.add(currentMerge)
                    currentMerge = nextRange
                }
                uiStateUpdater.update {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                            partialSolution = mergedRanges.solution(),
                        )
                    )
                }
                extraLongDelay(DelayReason.Solver)
            }
            mergedRanges.add(currentMerge)

            uiStateUpdater.update {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                        solution = mergedRanges.solution(),
                    )
                )
            }
            regularDelay(DelayReason.Solver)
        }
    }

    private fun List<LongRange>.solution(): String =
        sumOf { mergedRange -> mergedRange.last - mergedRange.first + 1L }.toString()
}