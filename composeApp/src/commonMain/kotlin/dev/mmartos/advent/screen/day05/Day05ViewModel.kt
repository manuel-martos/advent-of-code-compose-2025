package dev.mmartos.advent.screen.day05

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.threadSafeUpdate
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

data class IngredientsDatabase(
    val freshIDs: PersistentList<LongRange>,
    val availableIngredients: PersistentList<Long>,
)

data class IngredientState(
    val ingredientId: Long,
    val isFresh: Boolean,
)

sealed class ParserStage {
    data class Parsing(
        val currentLine: String,
        val partialDatabase: IngredientsDatabase,
    ) : ParserStage()

    data class Parsed(
        val database: IngredientsDatabase,
    ) : ParserStage()

    data object Error : ParserStage()
}

sealed class SolverStage1 {
    data class Solving(
        val verifiedIngredients: PersistentList<IngredientState>,
        val partialSolution: String,
    ) : SolverStage1()

    data class Solved(
        val verifiedIngredients: PersistentList<IngredientState>,
        val solution: String
    ) : SolverStage1()
}

sealed class SolverStage2 {
    data class Solving(
        val verifiedFreshIDs: PersistentList<LongRange>,
        val partialSolution: String,
    ) : SolverStage2()

    data class Solved(
        val verifiedFreshIDs: PersistentList<LongRange>,
        val solution: String
    ) : SolverStage2()
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
                _uiState.update {
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
                delay(5)
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
        _uiState.value.parsedData?.run {
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
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage1 = SolverStage1.Solving(
                            verifiedIngredients = verifiedIngredients.toPersistentList(),
                            partialSolution = result.toString(),
                        )
                    )
                }
                delay(2)
            }
        }
        _uiState.threadSafeUpdate {
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
        _uiState.value.parsedData?.run {
            val sortedRanges = freshIDs.sortedBy { it.first }
            val mergedRanges = mutableListOf<LongRange>()
            var currentMerge = sortedRanges.first()
            verifiedFreshIDs.add(currentMerge)
            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage2 = SolverStage2.Solving(
                        verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                        partialSolution = mergedRanges.solution(),
                    )
                )
            }
            delay(5)

            for (i in 1 until sortedRanges.size) {
                val nextRange = sortedRanges[i]
                verifiedFreshIDs.add(nextRange)
                if (nextRange.first <= currentMerge.last + 1) {
                    currentMerge = currentMerge.first..maxOf(currentMerge.last, nextRange.last)
                } else {
                    mergedRanges.add(currentMerge)
                    currentMerge = nextRange
                }
                _uiState.threadSafeUpdate {
                    it.copy(
                        solverStage2 = SolverStage2.Solving(
                            verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                            partialSolution = mergedRanges.solution(),
                        )
                    )
                }
                delay(20)
            }
            mergedRanges.add(currentMerge)

            _uiState.threadSafeUpdate {
                it.copy(
                    solverStage2 = SolverStage2.Solved(
                        verifiedFreshIDs = verifiedFreshIDs.toPersistentList(),
                        solution = mergedRanges.solution(),
                    )
                )
            }
            delay(5)
        }
    }

    private fun List<LongRange>.solution(): String =
        sumOf { mergedRange -> mergedRange.last - mergedRange.first + 1L }.toString()
}