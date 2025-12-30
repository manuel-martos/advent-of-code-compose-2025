package dev.mmartos.advent.screen.day02

import dev.mmartos.advent.common.BaseViewModel
import dev.mmartos.advent.common.ErrorStage
import dev.mmartos.advent.common.ParsedStage
import dev.mmartos.advent.common.ParsingStage
import dev.mmartos.advent.common.SolvedStage
import dev.mmartos.advent.common.SolverPart
import dev.mmartos.advent.common.SolvingStage
import dev.mmartos.advent.common.UiState
import dev.mmartos.advent.utils.Delay.longDelay
import dev.mmartos.advent.utils.DelayReason
import kotlin.math.ceil
import kotlin.math.log10
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import dev.mmartos.advent.common.ParserStage as BaseParserStage
import dev.mmartos.advent.common.SolverStage as BaseSolverStage

sealed class ParserStage : BaseParserStage {
    data class Parsing(
        val currentItem: String,
        val productIDRanges: PersistentList<LongRange>,
    ) : ParserStage(), ParsingStage

    data class Parsed(
        val productIDRanges: PersistentList<LongRange>,
    ) : ParserStage(), ParsedStage

    data object Error : ParserStage(), ErrorStage
}

sealed class SolverStage1 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_1

    data class Solving(
        val currentRange: LongRange,
        val partialSolution: String,
    ) : SolverStage1(), SolvingStage

    data class Solved(
        val solution: String
    ) : SolverStage1(), SolvedStage
}

sealed class SolverStage2 : BaseSolverStage {
    override fun solverPart(): SolverPart = SolverPart.SOLVER_PART_2

    data class Solving(
        val currentRange: LongRange,
        val partialSolution: String,
    ) : SolverStage2(), SolvingStage

    data class Solved(
        val solution: String
    ) : SolverStage2(), SolvedStage
}

typealias Day02UiState = UiState<ParserStage, SolverStage1, SolverStage2>

class Day02ViewModel : BaseViewModel<ParserStage, List<LongRange>, SolverStage1, SolverStage2>() {

    override val Day02UiState.parsedData: List<LongRange>?
        get() = (uiState.value.parserStage as? ParserStage.Parsed)?.productIDRanges

    fun startParser(input: List<String>) = doParsing {
        val productIDRanges = mutableListOf<LongRange>()
        runCatching {
            input.joinToString(",")
                .split(",")
                .forEach { item ->
                    val values = item.split("-")
                    val productIDRange = values.first().toLong()..values.last().toLong()
                    productIDRanges += productIDRange
                    uiStateUpdater.update {
                        it.copy(
                            parserStage = ParserStage.Parsing(
                                currentItem = item,
                                productIDRanges = productIDRanges.toPersistentList(),
                            )
                        )
                    }
                    longDelay(DelayReason.Parser)
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
                        productIDRanges = productIDRanges.toPersistentList(),
                    ),
                )
            }
            solvePart1()
            solvePart2()
        }
    }

    fun solvePart1() = doSolving {
        var result = 0L
        uiState.value.parsedData?.forEach { currentRange ->
            for (cur in currentRange) {
                val length = ceil(log10(cur.toDouble())).toInt()
                if (length % 2 == 0) {
                    val text = cur.toString()
                    val split1 = text.take(length / 2)
                    val split2 = text.substring(length / 2)
                    if (split1 == split2) {
                        result += cur
                        uiStateUpdater.update {
                            it.copy(
                                solverStage1 = SolverStage1.Solving(
                                    currentRange = currentRange,
                                    partialSolution = result.toString(),
                                )
                            )
                        }
                    }
                }
            }
            longDelay(DelayReason.Solver)
        }
        uiStateUpdater.update {
            it.copy(
                solverStage1 = SolverStage1.Solved(
                    solution = result.toString(),
                )
            )
        }
    }

    fun solvePart2() = doSolving {
        var result = 0L
        uiState.value.parsedData?.forEach { currentRange ->
            for (cur in currentRange) {
                val text = cur.toString()
                val textLength = text.length
                val isInvalid = (1..textLength / 2)
                    .any { curLength ->
                        val slice = text.take(curLength)
                        textLength % curLength == 0 &&
                                text.windowed(curLength, curLength, false).all { it == slice }
                    }
                if (isInvalid) {
                    result += cur
                    uiStateUpdater.update {
                        it.copy(
                            solverStage2 = SolverStage2.Solving(
                                currentRange = currentRange,
                                partialSolution = result.toString(),
                            )
                        )
                    }
                }
            }
            longDelay(DelayReason.Solver)
        }
        uiStateUpdater.update {
            it.copy(
                solverStage2 = SolverStage2.Solved(
                    solution = result.toString(),
                )
            )
        }
    }
}